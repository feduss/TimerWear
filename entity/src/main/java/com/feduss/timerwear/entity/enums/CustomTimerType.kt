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
}