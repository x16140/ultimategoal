package io.arct.techno.ftc.cv

enum class RingState(val rings: Int) {
    None(0),
    Partial(1),
    Full(4);

    override fun toString(): String =
        "RingState($rings)"
}