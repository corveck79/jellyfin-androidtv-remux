package org.jellyfin.androidtv.ui.presentation

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.leanback.widget.RowPresenter
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.DetailRowView
import org.jellyfin.androidtv.ui.itemdetail.MyDetailsOverviewRow
import org.jellyfin.androidtv.util.InfoLayoutHelper
import org.jellyfin.androidtv.util.MarkdownRenderer
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType
import kotlin.math.roundToInt

class MyDetailsOverviewRowPresenter(
	private val markdownRenderer: MarkdownRenderer,
) : RowPresenter() {
	class ViewHolder(
		private val detailRowView: DetailRowView,
		private val markdownRenderer: MarkdownRenderer,
	) : RowPresenter.ViewHolder(detailRowView) {
		private val binding get() = detailRowView.binding

		fun setItem(row: MyDetailsOverviewRow) {
			val episodeNumber = row.item.indexNumber?.takeIf { row.item.type == BaseItemKind.EPISODE }
			setTitle(if (episodeNumber != null) "$episodeNumber. ${row.item.name}" else row.item.name)
			binding.fdSubtitle.text = row.item.seasonName?.takeIf { row.item.type == BaseItemKind.EPISODE }?.let { "- $it" }
			binding.fdSubtitle.isVisible = !binding.fdSubtitle.text.isNullOrEmpty()

			InfoLayoutHelper.addInfoRow(view.context, row.item, row.item.mediaSources?.getOrNull(row.selectedMediaSourceIndex), binding.fdMainInfoRow, false)
			binding.fdGenreRow.text = row.item.genres?.joinToString(" / ")

			binding.infoTitle1.text = row.infoItem1?.label
			binding.infoValue1.text = row.infoItem1?.value

			binding.infoTitle2.text = row.infoItem2?.label
			binding.infoValue2.text = row.infoItem2?.value

			binding.infoTitle3.text = row.infoItem3?.label
			binding.infoValue3.text = row.infoItem3?.value

			binding.mainImage.load(row.imageDrawable, null, null, 1.0, 0)

			setSummary(row.summary)

			if (row.item.type == BaseItemKind.PERSON) {
				binding.fdSummaryText.maxLines = 9
				binding.fdGenreRow.isVisible = false
			}

			setSource(row)

			binding.fdButtonRow.removeAllViews()
			for ((index, button) in row.actions.withIndex()) {
				// Only the primary (play/resume) button keeps its label, the rest are plain icons
				button.labelAlwaysVisible = index == 0
				val parent = button.parent
				if (parent is ViewGroup) parent.removeView(button)

				binding.fdButtonRow.addView(button)
			}
		}

		private fun setSource(row: MyDetailsOverviewRow) {
			val sources = row.item.mediaSources.orEmpty()
			val source = sources.getOrNull(row.selectedMediaSourceIndex)
			binding.fdSourceCard.isVisible = source != null
			if (source == null) return

			binding.fdSourceText.text = source.name
			binding.fdMediaInfoText.text = describeMediaSource(source)

			setStreamPickers(row, source)

			val picker = binding.fdSourcePicker
			if (sources.size > 1) {
				picker.isFocusable = true
				picker.setOnClickListener { row.onSourceClick?.invoke(it) }
			} else {
				picker.isFocusable = false
				picker.setOnClickListener(null)
			}
		}

		private fun setStreamPickers(row: MyDetailsOverviewRow, source: MediaSourceInfo) {
			val streams = source.mediaStreams.orEmpty()
			val audio = streams.filter { it.type == MediaStreamType.AUDIO }
			val subtitles = streams.filter { it.type == MediaStreamType.SUBTITLE }

			binding.fdStreamPickers.isVisible = audio.size > 1 || subtitles.isNotEmpty()
			binding.fdAudioPicker.isVisible = audio.size > 1
			binding.fdSubtitlePicker.isVisible = subtitles.isNotEmpty()

			val audioIndex = row.selectedAudioIndex ?: source.defaultAudioStreamIndex ?: audio.firstOrNull()?.index
			binding.fdAudioText.text = audio.firstOrNull { it.index == audioIndex }?.let(::streamTitle)
			binding.fdAudioPicker.setOnClickListener { row.onAudioClick?.invoke(it) }

			val subtitleIndex = row.selectedSubtitleIndex ?: source.defaultSubtitleStreamIndex
			binding.fdSubtitleText.text = subtitles.firstOrNull { it.index == subtitleIndex }?.let(::streamTitle)
				?: binding.root.context.getString(R.string.lbl_none)
			binding.fdSubtitlePicker.setOnClickListener { row.onSubtitleClick?.invoke(it) }
		}

		fun setTitle(title: String?) {
			binding.fdTitle.text = title
		}

		fun setSummary(summary: String?) {
			binding.fdSummaryText.text = summary?.let { markdownRenderer.toMarkdownSpanned(it) }
		}

		fun setInfoValue3(text: String?) {
			binding.infoValue3.text = text
		}
	}

	var viewHolder: ViewHolder? = null
		private set

	init {
		syncActivatePolicy = SYNC_ACTIVATED_CUSTOM
	}

	override fun createRowViewHolder(parent: ViewGroup): ViewHolder {
		val view = DetailRowView(parent.context)
		viewHolder = ViewHolder(view, markdownRenderer)
		return viewHolder!!
	}

	override fun onBindRowViewHolder(viewHolder: RowPresenter.ViewHolder, item: Any) {
		super.onBindRowViewHolder(viewHolder, item)
		if (item !is MyDetailsOverviewRow) return
		if (viewHolder !is ViewHolder) return

		viewHolder.setItem(item)
	}

	override fun onSelectLevelChanged(holder: RowPresenter.ViewHolder) = Unit
}

fun streamTitle(stream: MediaStream): String =
	stream.displayTitle?.takeIf { it.isNotBlank() }
		?: stream.language?.takeIf { it.isNotBlank() }
		?: stream.title?.takeIf { it.isNotBlank() }
		?: "Track ${stream.index}"

/** Summarises the video stream and play method, e.g. "1080p H264 6.3 Mbps (Direct Play)". */
private fun describeMediaSource(source: MediaSourceInfo): String {
	val video = source.mediaStreams?.firstOrNull { it.type == MediaStreamType.VIDEO }
	val parts = buildList {
		video?.height?.let { add("${it}p") }
		video?.codec?.let { add(it.uppercase()) }
		(video?.bitRate ?: source.bitrate)?.let { add("%.1f Mbps".format(it / 1_000_000.0)) }
	}
	val method = when {
		source.supportsDirectPlay -> "Direct Play"
		source.supportsDirectStream -> "Direct Stream"
		else -> "Transcode"
	}
	return (parts + "($method)").joinToString(" ")
}
