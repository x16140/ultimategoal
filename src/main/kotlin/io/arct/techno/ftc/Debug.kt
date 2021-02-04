package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo

@OperationMode.Register(OperationMode.Type.Operated, name = "Debug")
class Debug : OperationMode() {
    private val m1 by Motor
    private val m2 by Motor
    private val m3 by Motor
    private val m4 by Motor
    private val m5 by Motor
    private val m6 by Motor
    private val m7 by Motor
    private val m8 by Motor

    private val s1 by Servo // shooter
    private val s2 by Servo // wooble

    private val gamepad0 by Controller
    private val gamepad1 by Controller

    private val mla = listOf(m1, m2, m3, m4)
    private val mlb = listOf(m5, m6, m7, m8)

    private var sp = -1.0

    var ma = 0
    var mb = 0
    var mc = 0
    var md = 0

    override suspend fun loop() {
        log
            .add("Currently Selected:")
            .add("A1: Motor ${ma + 1}")
            .add("A2: Motor ${mb + 1}")
            .add("B1: Motor ${mc + 5}")
            .add("B2: Motor ${md + 5}")
            .update()

        setIndexA()
        setIndexB()

        mla[ma].power(gamepad0.left.y)
        mla[mb].power(gamepad0.right.y)
        mlb[mc].power(gamepad1.left.y)
        mlb[md].power(gamepad1.right.y)

        if (gamepad0.rb) {
            s1.position = -1.0

            Thread.sleep(100)
        }

        s1.position = 1.0

        if (gamepad0.lb) {
            sp = if (sp == 1.0) -1.0 else 1.0

            while (gamepad0.lb);
        }

        s2.position = sp
    }

    fun setIndexA() {
        if (gamepad0.dpad.up) {
            ma = 0
            while (gamepad0.dpad.up);
        }

        if (gamepad0.dpad.left) {
            ma = 1
            while (gamepad0.dpad.left);
        }

        if (gamepad0.dpad.right) {
            ma = 2
            while (gamepad0.dpad.right);
        }

        if (gamepad0.dpad.down) {
            ma = 3
            while (gamepad0.dpad.down);
        }

        if (gamepad0.y) {
            mb = 0
            while (gamepad0.y);
        }

        if (gamepad0.x) {
            mb = 1
            while (gamepad0.x);
        }

        if (gamepad0.b) {
            mb = 2
            while (gamepad0.b);
        }

        if (gamepad0.a) {
            mb = 3
            while (gamepad0.a);
        }
    }

    fun setIndexB() {
        if (gamepad1.dpad.up) {
            mc = 0
            while (gamepad1.dpad.up);
        }

        if (gamepad1.dpad.left) {
            mc = 1
            while (gamepad1.dpad.left);
        }

        if (gamepad1.dpad.right) {
            mc = 2
            while (gamepad1.dpad.right);
        }

        if (gamepad1.dpad.down) {
            mc = 3
            while (gamepad1.dpad.down);
        }

        if (gamepad1.y) {
            md = 0
            while (gamepad1.y);
        }

        if (gamepad1.x) {
            md = 1
            while (gamepad1.x);
        }

        if (gamepad1.b) {
            md = 2
            while (gamepad1.b);
        }

        if (gamepad1.a) {
            md = 3
            while (gamepad1.a);
        }
    }
}