package org.jellyfin.androidtv.ui.playback

import java.util.UUID

/**
 * Audio and subtitle tracks chosen on the details page, handed to the player when it starts that
 * item. Indexes refer to the media source that was selected there. A subtitle index of -1 means
 * "no subtitles". The selection only applies to [itemId] and expires after [MAX_AGE_MS].
 */
object PlaybackSelection {
	private const val MAX_AGE_MS = 10 * 60 * 1000L

	data class Selection(
		val itemId: UUID,
		val audioStreamIndex: Int?,
		val subtitleStreamIndex: Int?,
		val createdAt: Long = System.currentTimeMillis(),
	)

	private var current: Selection? = null

	@JvmStatic
	fun set(itemId: UUID, audioStreamIndex: Int?, subtitleStreamIndex: Int?) {
		current = if (audioStreamIndex == null && subtitleStreamIndex == null) null
		else Selection(itemId, audioStreamIndex, subtitleStreamIndex)
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
