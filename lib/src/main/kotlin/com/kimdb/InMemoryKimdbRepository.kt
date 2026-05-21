package com.kimdb

import com.kimdb.api.KimdbRepository
import com.kimdb.model.Name
import com.kimdb.model.NConst
import com.kimdb.model.Title
import com.kimdb.model.TConst
import com.kimdb.model.TitleType

class InMemoryKimdbRepository : KimdbRepository {
    override fun getTitlesByPrimaryTitle(movieTitleAsString: String): List<Title> = emptyList()

    override fun getNamesByPrimaryName(nameAsString: String): List<Name> = emptyList()

    override fun getTitle(id: TConst): Title? = null

    override fun getName(id: NConst): Name? = null

    override fun getTitlesByTypeAndLength(titleType: TitleType, length: Int): List<Title> = emptyList()

    override fun getTitles(): Sequence<Title> = emptySequence()

    override fun getNames(): Sequence<Name> = emptySequence()
}