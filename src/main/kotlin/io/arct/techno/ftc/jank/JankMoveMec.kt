package io.arct.techno.ftc.jank


import io.arct.rl.robot.Robot
import io.arct.rl.units.Angle
import io.arct.rl.units.Distance
import io.arct.rl.units.Velocity

class JankMoveMec(private val robot: Robot, var state: Boolean) {
    fun move(direction: Angle, speed: Velocity, rotationSpeed: Velocity) {
        if (state) {
            robot.move(direction, speed)
        } else {
            robot.rotate(rotationSpeed)
        }

        state = !state
    }
}