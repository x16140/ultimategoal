package io.arct.techno.ftc.util

import io.arct.rl.units.Quantity

fun Double.map(from: ClosedFloatingPointRange<Double>, to: ClosedFloatingPointRange<Double>) =
    (this - from.start) * (to.endInclusive - to.start) / (from.endInclusive - from.start) + from.start