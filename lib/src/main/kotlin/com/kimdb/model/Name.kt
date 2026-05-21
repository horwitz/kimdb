package com.kimdb.model

data class Name(
    val nconst: NConst,
    val primaryName: String,
    val birthYear: Int?,
    val deathYear: Int?,
    val primaryProfession: Set<String>,
    val knownForTitles: Set<TConst>,
)