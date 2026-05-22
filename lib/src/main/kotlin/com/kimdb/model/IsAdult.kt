package com.kimdb.model

enum class IsAdult(
    val imdbValue: String
) {
    ADULT("1"),
    NOT_ADULT("0")
    ;

    companion object {
        private val byImdbValue = entries.associateBy(IsAdult::imdbValue)

        fun of(imdbValue: String) = byImdbValue[imdbValue] ?: throw IllegalArgumentException("Unknown isAdult value: $imdbValue")
    }
}
