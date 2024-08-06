package com.feduss.timerwear.entity.enums

enum class CustomTimerType {
    Work,
    Rest;

    override fun toString(): String {
        return when (this) {
            Work -> "Work"
            Rest -> "Rest"
        }
    }
}