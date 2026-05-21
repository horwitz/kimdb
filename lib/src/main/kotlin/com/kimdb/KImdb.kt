package com.kimdb

import com.kimdb.api.KImdbRepository
import com.kimdb.model.Name
import com.kimdb.model.Title
import com.kimdb.tsv.ImdbTsvParser
import java.nio.file.Path

object KImdb {
    fun inMemoryRepository(
        titles: List<Title> = emptyList(),
        names: List<Name> = emptyList(),
    ): KImdbRepository = InMemoryKImdbRepository(titles, names)

    fun repositoryFromTsv(
        titleBasicsPath: Path,
        nameBasicsPath: Path,
    ): KImdbRepository = ImdbTsvParser.loadRepository(titleBasicsPath, nameBasicsPath)
}
