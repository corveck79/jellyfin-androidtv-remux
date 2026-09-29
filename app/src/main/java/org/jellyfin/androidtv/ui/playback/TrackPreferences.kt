package org.jellyfin.androidtv.ui.playback

import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType

/**
 * Default audio and subtitle tracks: English when the source has it, and never Russian unless
 * there is nothing else. Shared by the details page (what it shows) and the player (what starts).
 */
object TrackPreferences {
	private val ENGLISH = setOf("eng", "en")
	private val RUSSIAN = setOf("rus", "ru")

	/** Audio track index (or null) and subtitle track index (-1 means no subtitles). */
	data class Tracks(val audio: Int?, val subtitle: Int)

	@JvmStatic
	fun isRussian(stream: MediaStream): Boolean =
		stream.language?.lowercase() in RUSSIAN ||
			listOfNotNull(stream.displayTitle, stream.title).any { it.contains("russian", ignoreCase = true) }

	@JvmStatic
	fun isEnglish(stream: MediaStream): Boolean =
		stream.language?.lowercase() in ENGLISH ||
			(stream.language == null && listOfNotNull(stream.displayTitle, stream.title).any { it.contains("english", ignoreCase = true) })

	/** True when [index] points at a Russian track in [streams]. */
	@JvmStatic
	fun isRussianStream(streams: List<MediaStream>?, index: Int?): Boolean =
		index != null && streams?.firstOrNull { it.index == index }?.let(::isRussian) == true

	/** English first, then the server default unless it is Russian, then the first non-Russian track. */
	@JvmStatic
	fun defaultAudio(streams: List<MediaStream>?, serverDefault: Int?): Int? {
		val audio = streams.orEmpty().filter { it.type == MediaStreamType.AUDIO }
		val english = audio.filter(::isEnglish)
		english.firstOrNull { it.isDefault }?.let { return it.index }
		english.firstOrNull()?.let { return it.index }

		val server = audio.firstOrNull { it.index == serverDefault }
		if (server != null && !isRussian(server)) return server.index
		audio.firstOrNull { !isRussian(it) }?.let { return it.index }
		return serverDefault ?: audio.firstOrNull()?.index
	}

	/** No subtitles when the audio is English, otherwise full English subtitles if available. */
	@JvmStatic
	fun defaultSubtitle(streams: List<MediaStream>?, audioIndex: Int?): Int {
		val list = streams.orEmpty()
		if (list.firstOrNull { it.type == MediaStreamType.AUDIO && it.index == audioIndex }?.let(::isEnglish) == true) return -1

		val english = list.filter { it.type == MediaStreamType.SUBTITLE && isEnglish(it) && !it.isForced }
		return (english.firstOrNull { it.isDefault } ?: english.firstOrNull())?.index ?: -1
	}

	/** Applies the defaults on top of what the user picked on the details page (null = not picked). */
	@JvmStatic
	fun resolve(source: MediaSourceInfo, pickedAudio: Int?, pickedSubtitle: Int?): Tracks {
		val audio = pickedAudio ?: defaultAudio(source.mediaStreams, source.defaultAudioStreamIndex)
		val subtitle = pickedSubtitle ?: defaultSubtitle(source.mediaStreams, audio)
		return Tracks(audio, subtitle)
	}
}
