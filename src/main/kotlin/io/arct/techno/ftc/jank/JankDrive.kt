package io.arct.techno.ftc.jank

import io.arct.rl.control.Control
import io.arct.rl.extensions.normalize
import io.arct.rl.hardware.input.Controller
import io.arct.rl.robot.drive.IDrive
import io.arct.rl.robot.drive.MecanumDrive
import io.arct.rl.robot.position.DynamicPositioning
import io.arct.rl.units.Angle
import io.arct.rl.units.Velocity
import io.arct.rl.units.deg
import kotlin.math.*

class LessJankDrive(drive: IDrive) : Control(drive) {
    override fun apply(controller: Controller) {
        val joystick = controller.left

        if ( +controller.rb || +controller.lb || controller.rt != .0 || controller.lt != .0 ){}
        else if (controller.left.origin && controller.right.origin)
            drive.stop()
        else if (controller.left.origin)
            drive.rotate(
//                drive.velocity * controller.left.y,
                drive.velocity * controller.right.x
            )
        else {

            if (controller.right.x == .0)
            drive.move(
                Angle.fromCoordinates(
                    -joystick.x,
                    joystick.y
                ) ?: return,

                drive.velocity * min(sqrt(joystick.x.pow(2) + joystick.y.pow(2)), 1.0)
            )

            else
                drive.turn(
                    drive.velocity * joystick.y,
                    drive.velocity * controller.right.x
                )

        }
    }
}