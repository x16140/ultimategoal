package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.input.Controller
import io.arct.rl.units.*
import kotlin.math.abs
import kotlin.math.sign

@OperationMode.Register(OperationMode.Type.Autonomous, "Test Program")
class ImuTesting : LinearOperationMode() {
    val gamepad = Controller.get("gamepad0")

    override suspend fun run() {
        while (!gamepad.a) {
            idle()
        }

        correct()
    }

    private fun correct() {

    }
}