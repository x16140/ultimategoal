package io.arct.techno.ftc.cv

data class Color(val h: Double, val s: Double, val v: Double) {
    override fun toString(): String = "hsv($h, $s, $v)"
}
