package com.kimdb.api

import com.kimdb.model.NConst
import com.kimdb.model.Name
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType

interface KImdbRepository {
    fun getTitlesByPrimaryTitle(movieTitleAsString: String): List<Title>

    fun getNamesByPrimaryName(nameAsString: String): List<Name>

    fun getTitle(id: TConst): Title?

    fun getName(id: NConst): Name?

    fun getTitlesByTypeAndLength(
        titleType: TitleType,
        length: Int,
    ): List<Title>

    fun getTitles(): List<Title>

    fun getNames(): List<Name>
}
