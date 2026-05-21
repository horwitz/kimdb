package com.kimdb.model

@JvmInline
value class TConst private constructor(val value: String) {
    companion object {
        fun of(value: String): TConst {
            require(value.matches(Regex("tt\\d+"))) { "Invalid tconst: $value" }
            return TConst(value)
        }
    }

    override fun toString(): String = value
}