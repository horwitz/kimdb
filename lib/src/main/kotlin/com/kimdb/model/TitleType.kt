package com.kimdb.model

enum class TitleType(
    val imdbValue: String,
) {
    MOVIE("movie"),
    SHORT("short"),
    TV_EPISODE("tvEpisode"),
    TV_MOVIE("tvMovie"),
    TV_SERIES("tvSeries"),
    TV_SHORT("tvShort"),
    TV_SPECIAL("tvSpecial"),
    VIDEO("video"),
    VIDEO_GAME("videoGame"),
    TV_MINI_SERIES("tvMiniSeries"),
    TV_PILOT("tvPilot"),
    ;

    companion object {
        private val byImdbValue = entries.associateBy(TitleType::imdbValue)

        fun of(imdbValue: String) =
            byImdbValue[imdbValue] ?: throw IllegalArgumentException("Unknown title type: $imdbValue")
    }
}
