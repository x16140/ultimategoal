package io.arct.techno.ftc.cv

enum class AutonomousState(val state: RingState?) {
    Detect(null),
    None(RingState.None),
    Partial(RingState.Partial),
    Full(RingState.Full);

    override fun toString(): String =
        state?.toString() ?: "Automatic Detection"
}