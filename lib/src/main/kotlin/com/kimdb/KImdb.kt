package com.kimdb

import com.kimdb.api.KImdbRepository
import com.kimdb.model.Name
import com.kimdb.model.Title
import com.kimdb.tsv.ImdbTsvParser
import java.io.InputStream
import java.nio.file.Path

object KImdb {
    fun inMemoryRepository(
        titles: List<Title>,
        names: List<Name>
    ): KImdbRepository = InMemoryKImdbRepository(titles, names)

    fun inMemoryRepositoryFromTsv(
        titleBasicsPath: Path,
        nameBasicsPath: Path
    ): KImdbRepository = ImdbTsvParser.loadRepository(titleBasicsPath, nameBasicsPath)

    fun inMemoryRepositoryFromTsv(
        titleBasicsStream: InputStream,
        nameBasicsStream: InputStream
    ): KImdbRepository = ImdbTsvParser.loadRepository(titleBasicsStream, nameBasicsStream)

    fun sqliteRepository(
        dbPath: Path
    ): KImdbRepository = SqliteKImdbRepository(dbPath)
}
