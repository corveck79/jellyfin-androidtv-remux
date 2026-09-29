package org.jellyfin.androidtv.ui.playback

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType

class TrackPreferencesTest : FunSpec({
	fun stream(
		index: Int,
		type: MediaStreamType,
		language: String?,
		isDefault: Boolean = false,
		isForced: Boolean = false,
	) = MediaStream(
		index = index,
		type = type,
		language = language,
		isDefault = isDefault,
		isForced = isForced,
		isInterlaced = false,
		isHearingImpaired = false,
		isExternal = false,
		isTextSubtitleStream = false,
		supportsExternalStream = false,
	)

	fun audio(index: Int, language: String?, isDefault: Boolean = false) = stream(index, MediaStreamType.AUDIO, language, isDefault)
	fun sub(index: Int, language: String?, isForced: Boolean = false) = stream(index, MediaStreamType.SUBTITLE, language, isForced = isForced)

	test("audio prefers English over a Russian server default") {
		val streams = listOf(audio(1, "rus", isDefault = true), audio(2, "eng"))

		TrackPreferences.defaultAudio(streams, serverDefault = 1) shouldBe 2
	}

	test("audio without English skips Russian and keeps a non-Russian server default") {
		val streams = listOf(audio(1, "rus"), audio(2, "fra"), audio(3, "deu"))

		TrackPreferences.defaultAudio(streams, serverDefault = 1) shouldBe 2
		TrackPreferences.defaultAudio(streams, serverDefault = 3) shouldBe 3
	}

	test("audio falls back to the server default when only Russian exists") {
		val streams = listOf(audio(1, "rus"))

		TrackPreferences.defaultAudio(streams, serverDefault = 1) shouldBe 1
	}

	test("no subtitles when the audio is English") {
		val streams = listOf(audio(1, "eng"), sub(2, "eng"), sub(3, "rus"))

		TrackPreferences.defaultSubtitle(streams, audioIndex = 1) shouldBe -1
	}

	test("English subtitles when the audio is not English, never Russian or forced") {
		val streams = listOf(audio(1, "fra"), sub(2, "rus"), sub(3, "eng", isForced = true), sub(4, "eng"))

		TrackPreferences.defaultSubtitle(streams, audioIndex = 1) shouldBe 4
	}

	test("no subtitles when there are no English ones") {
		val streams = listOf(audio(1, "fra"), sub(2, "rus"), sub(3, "deu"))

		TrackPreferences.defaultSubtitle(streams, audioIndex = 1) shouldBe -1
	}

	test("Russian is recognised by language code and by title") {
		TrackPreferences.isRussian(audio(1, "rus")) shouldBe true
		TrackPreferences.isRussian(audio(1, "ru")) shouldBe true
		TrackPreferences.isRussian(audio(1, "eng")) shouldBe false
		TrackPreferences.isRussianStream(listOf(audio(1, "rus")), 1) shouldBe true
		TrackPreferences.isRussianStream(listOf(audio(1, "eng")), 1) shouldBe false
		TrackPreferences.isRussianStream(null, 1) shouldBe false
	}
})
