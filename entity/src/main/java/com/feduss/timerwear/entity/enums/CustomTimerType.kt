package com.feduss.timerwear.entity.enums

enum class CustomTimerType {
    Work,
    Rest,
    IntermediumRest;

    override fun toString(): String {
        return when (this) {
            Work -> "Work"
            Rest -> "Rest"
            IntermediumRest -> "IntermediumRest"
        }
    }

    companion object {
        fun fromString(raw: String): CustomTimerType? {
            return when (raw) {
                Work.toString() -> Work
                Rest.toString() -> Work
                IntermediumRest.toString() -> Work
                else -> null
            }
        }
    }
}