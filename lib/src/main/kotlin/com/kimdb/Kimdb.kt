package com.kimdb

import com.kimdb.api.KimdbRepository
import com.kimdb.model.Name
import com.kimdb.model.Title
import com.kimdb.tsv.ImdbTsvParser
import java.nio.file.Path

object Kimdb {
    fun inMemoryRepository(
        titles: List<Title> = emptyList(),
        names: List<Name> = emptyList(),
    ): KimdbRepository = InMemoryKimdbRepository(titles, names)

    fun repositoryFromTsv(
        titleBasicsPath: Path,
        nameBasicsPath: Path,
    ): KimdbRepository = ImdbTsvParser.loadRepository(titleBasicsPath, nameBasicsPath)
}
