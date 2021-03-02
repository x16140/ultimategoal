package io.arct.techno.ftc.jank

import io.arct.rl.control.Control
import io.arct.rl.hardware.input.Controller
import io.arct.rl.robot.drive.IDrive
import io.arct.rl.units.Angle
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class JankDrive(drive: IDrive) : Control(drive) {
    private var state : Boolean = false
    override fun apply(controller: Controller) {
        val joystick = controller.left

        if (state) {
            drive.move(
                    Angle.fromCoordinates(joystick.x, -joystick.y) ?: return,
                    drive.velocity * min(sqrt(joystick.x.pow(2) + joystick.y.pow(2)), 1.0)
            )
        } else {
            drive.rotate(drive.velocity * controller.right.x)
        }

        state = !state
    }
}

class LessJankDrive(drive: IDrive) : Control(drive) {
    override fun apply(controller: Controller) {
        val joystick = controller.left

        if (controller.left.x != .0)

            drive.move(
                    Angle.fromCoordinates(
                            joystick.x,
                            -joystick.y
                    ) ?: return,

                    drive.velocity * min(sqrt(joystick.x.pow(2) + joystick.y.pow(2)), 1.0)
            )

            else
        drive.turn(
                drive.velocity * controller.left.y,
                drive.velocity * controller.right.x
        )
    }
}

// there is no mecanum part
//it just alters mecanum
//like with the left joystick
//the bot can't move it can only spot turn
//bascailly i just want rotation with translation, imo this is the easiest way to do it