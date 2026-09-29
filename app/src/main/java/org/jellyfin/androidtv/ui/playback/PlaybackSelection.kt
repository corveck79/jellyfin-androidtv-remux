package org.jellyfin.androidtv.ui.playback

import java.util.UUID

/**
 * Version (media source) and audio/subtitle tracks chosen on the details page, handed to the
 * player when it starts that item. Track indexes refer to the media source selected there. A subtitle index of -1 means
 * "no subtitles". The selection only applies to [itemId] and expires after [MAX_AGE_MS].
 */
object PlaybackSelection {
	private const val MAX_AGE_MS = 10 * 60 * 1000L

	data class Selection(
		val itemId: UUID,
		val mediaSourceId: String?,
		val audioStreamIndex: Int?,
		val subtitleStreamIndex: Int?,
		val createdAt: Long = System.currentTimeMillis(),
	)

	private var current: Selection? = null

	@JvmStatic
	fun set(itemId: UUID, mediaSourceId: String?, audioStreamIndex: Int?, subtitleStreamIndex: Int?) {
		current = if (mediaSourceId == null && audioStreamIndex == null && subtitleStreamIndex == null) null
		else Selection(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex)
	}

	@JvmStatic
	fun clear() {
		current = null
	}

	@JvmStatic
	fun get(itemId: UUID?): Selection? = current?.takeIf {
		it.itemId == itemId && System.currentTimeMillis() - it.createdAt < MAX_AGE_MS
	}
}
