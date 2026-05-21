package com.kimdb

import com.kimdb.api.KImdbRepository
import com.kimdb.model.NConst
import com.kimdb.model.Name
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType

class InMemoryKImdbRepository(
    private val titles: List<Title> = emptyList(),
    private val names: List<Name> = emptyList(),
) : KImdbRepository {
    private val titlesByPrimaryTitle = titles.groupBy { it.primaryTitle }
    private val namesByPrimaryName = names.groupBy { it.primaryName }
    private val titleById = titles.associateBy { it.tconst }
    private val nameById = names.associateBy { it.nconst }
    private val titlesByTypeAndLength = titles.groupBy { it.titleType to it.primaryTitle.length }

    override fun getTitlesByPrimaryTitle(movieTitleAsString: String) = titlesByPrimaryTitle[movieTitleAsString].orEmpty()

    override fun getNamesByPrimaryName(nameAsString: String) = namesByPrimaryName[nameAsString].orEmpty()

    override fun getTitle(id: TConst) = titleById[id]

    override fun getName(id: NConst) = nameById[id]

    override fun getTitlesByTypeAndLength(
        titleType: TitleType,
        length: Int,
    ) = titlesByTypeAndLength[titleType to length].orEmpty()

    override fun getTitles() = titles

    override fun getNames() = names
}
