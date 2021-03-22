package io.arct.techno.ftc.jank

import io.arct.rl.robot.Robot
import io.arct.rl.units.*
import kotlin.math.*

class JankMoveTo(
    private val robot: Robot,

    private val dt: Distance,
    private val at: Angle
) {
    private fun d(x: Distance, y: Distance): Distance =
        sqrt((x - robot.position.x).cm.value.pow(2) + (y - robot.position.y).cm.value.pow(2)).cm

    private fun a(a: Angle): Angle =
        (robot.rotation.normal - a.normal).deg

    suspend operator fun invoke(x: Distance, y: Distance, speed: Velocity, angle: Angle? = null) {
        while (d(x, y) > dt) {
            val dx = x - robot.position.x
            val dy = y - robot.position.y

            val tt = atan(dx / dy)
            val t = if (tt.isNaN()) .0 else tt

            if (abs(t - robot.rotation.rad.value) > at.rad.value) {
                robot.rotate(speed * sign(t))
                continue
            }

            robot.move(Angle.Forward, speed)
        }

        if (angle != null)
            robot.rotate(a(angle), speed)

        robot.stop()
    }
}