package com.feduss.timerwear.entity.enums

enum class VibrationType {
    SingleShort,
    SingleLong,
    DoubleShort;

    fun toPattern(): LongArray {
        return when(this) {
            SingleShort -> longArrayOf(500)
            SingleLong -> longArrayOf(1000)
            DoubleShort -> longArrayOf(500, 500)
        }
    }
}