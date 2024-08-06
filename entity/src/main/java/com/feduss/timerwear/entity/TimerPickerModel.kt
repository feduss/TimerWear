package com.feduss.timerwear.entity

class TimerPickerModel(val minutes: Int, val seconds: Int) {

    override fun toString(): String {
        return "${minutes}m ${seconds}s"
    }

    fun toSeconds(): Int {
        return (minutes * 60) + seconds
    }
}