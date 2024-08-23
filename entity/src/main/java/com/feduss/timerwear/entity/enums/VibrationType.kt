package com.feduss.timerwear.entity.enums

enum class VibrationType {
    SingleVeryShort,
    SingleShort,
    SingleLong,
    DoubleShort;

    fun toPattern(): LongArray {
        return when(this) {
            SingleVeryShort -> longArrayOf(0, 100)
            SingleShort -> longArrayOf(0, 500)
            SingleLong -> longArrayOf(0, 1000)
            DoubleShort -> longArrayOf(0, 500, 0, 500)
        }
    }
}