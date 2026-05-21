package com.kimdb.model

@JvmInline
value class NConst private constructor(
    val value: String,
) {
    companion object {
        fun of(value: String): NConst {
            require(value.matches(Regex("nm\\d+"))) { "Invalid nconst: $value" }
            return NConst(value)
        }
    }

    override fun toString(): String = value
}
