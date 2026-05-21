package com.kimdb

import com.kimdb.api.KimdbRepository
import com.kimdb.model.NConst
import com.kimdb.model.Name
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType

class InMemoryKimdbRepository(
    titles: Sequence<Title> = emptySequence(),
    names: Sequence<Name> = emptySequence(),
) : KimdbRepository {
    private val titlesList = titles.toList()
    private val namesList = names.toList()

    private val titlesByPrimaryTitle = titlesList.groupBy { it.primaryTitle }
    private val namesByPrimaryName = namesList.groupBy { it.primaryName }
    private val titleById = titlesList.associateBy { it.tconst }
    private val nameById = namesList.associateBy { it.nconst }
    private val titlesByTypeAndLength = titlesList.groupBy { it.titleType to it.primaryTitle.length }

    override fun getTitlesByPrimaryTitle(movieTitleAsString: String) = titlesByPrimaryTitle[movieTitleAsString].orEmpty()

    override fun getNamesByPrimaryName(nameAsString: String) = namesByPrimaryName[nameAsString].orEmpty()

    override fun getTitle(id: TConst) = titleById[id]

    override fun getName(id: NConst) = nameById[id]

    override fun getTitlesByTypeAndLength(
        titleType: TitleType,
        length: Int,
    ) = titlesByTypeAndLength[titleType to length].orEmpty()

    override fun getTitles() = titlesList

    override fun getNames() = namesList
}
