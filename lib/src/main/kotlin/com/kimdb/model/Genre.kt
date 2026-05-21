package com.kimdb.model

enum class Genre(
    val imdbValue: String,
) {
    DOCUMENTARY("Documentary"),
    SHORT("Short"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    ROMANCE("Romance"),
    SPORT("Sport"),
    NEWS("News"),
    DRAMA("Drama"),
    FANTASY("Fantasy"),
    HORROR("Horror"),
    BIOGRAPHY("Biography"),
    MUSIC("Music"),
    WAR("War"),
    CRIME("Crime"),
    WESTERN("Western"),
    FAMILY("Family"),
    ADVENTURE("Adventure"),
    ACTION("Action"),
    HISTORY("History"),
    MYSTERY("Mystery"),
    SCI_FI("Sci-Fi"),
    MUSICAL("Musical"),
    THRILLER("Thriller"),
    FILM_NOIR("Film-Noir"),
    TALK_SHOW("Talk-Show"),
    GAME_SHOW("Game-Show"),
    REALITY_TV("Reality-TV"),
    ADULT("Adult"),
    EXPERIMENTAL("Experimental"),
    ;

    companion object {
        private val byImdbValue = entries.associateBy(Genre::imdbValue)

        fun of(imdbValue: String) =
            byImdbValue[imdbValue] ?: throw IllegalArgumentException("Unknown genre: $imdbValue")
    }
}
