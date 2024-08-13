package com.feduss.timerwear.entity.enums

enum class VibrationType {
    SingleShort,
    SingleLong,
    DoubleShort;

    fun toPattern(): LongArray {
        return when(this) {
            SingleShort -> longArrayOf(0, 500)
            SingleLong -> longArrayOf(0, 1000)
            DoubleShort -> longArrayOf(0, 500, 0, 500)
        }
    }
}