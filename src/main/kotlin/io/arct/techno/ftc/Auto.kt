package io.arct.techno.ftc

import io.arct.ftc.eventloop.LinearOperationMode
import io.arct.ftc.eventloop.OperationMode
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.motors.Motor
import io.arct.rl.hardware.motors.Servo
import io.arct.rl.robot.Robot
import io.arct.rl.units.*
import io.arct.techno.ftc.cv.AutonomousState
import io.arct.techno.ftc.cv.RingDetector
import io.arct.techno.ftc.cv.RingState
import io.arct.techno.ftc.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@OperationMode.Register(OperationMode.Type.Autonomous, "Auto")
class Auto : LinearOperationMode() {
    private val gamepad0 by Controller

    private val secrets: Secret = PersistentObject.load("/sdcard/secrets.dat")

    private val bot = bot()
    private val detector = RingDetector(this, secrets.vuforia)

    val q = 1.7.mps
    val k = 1.5.mps
    val f = 1.3.mps
    val s = .9.mps

    var mode = AutonomousState.Detect
    var saveScan = false

    override suspend fun init() {
        bot.wobble.lockAsync()
        bot.shooter.high()

        var done = false

        while (!done) gamepad0 {
            active {
                if (+gamepad0.rb) bot.wobble.releaseAsync() else bot.wobble.lockAsync()
            }

            click(Controller::back) {
                saveScan = true
                done = true
            }

            click(Controller::a) {
                done = true
            }

            click(Controller::x) {
                mode = AutonomousState.None
                done = true
            }

            click(Controller::y) {
                mode = AutonomousState.Partial
                done = true
            }

            click(Controller::b) {
                mode = AutonomousState.Full
                done = true
            }

            active {
                log.add("""(a) ${AutonomousState.Detect} (back: save)
                          |(x) ${AutonomousState.None}
                          |(y) ${AutonomousState.Partial}
                          |(b) ${AutonomousState.Full}""".trimMargin()
                ).update()
            }
        }

        log
            .clear()
            .add("Selected: $mode ${if (saveScan) "(Save)" else ""}")
            .update()
    }

    override suspend fun run(): Unit = with(bot) {
        log.clear().add("Scanning Rings...").update()

        val state = mode.state ?: detector.scan(saveScan)

        log.clear().add("Randomization State: $state (${if (mode == AutonomousState.Detect) 
            "Detected" 
        else 
            "Preselected"
        })").add("Running autonomous...").update()

//        GlobalScope.launch {
//            while (true)
//                log.clear()
//                    .add("y1: ${hardware.y1.position}")
//                    .add("y2: ${hardware.y2.position}")
//                    .add("x: ${hardware.x.position}")
//                    .update()
//        }

        accel(Angle.Forward, 148.cm, f)

        shooter.revup()
        move(Angle.Left, 40.cm, s)

        align()
        shooter.shoot(4)
        shooter.revdown()

        when (state) {
            RingState.None -> none()
            RingState.Partial -> partial()
            RingState.Full -> full()
        }

        log.add("Autonomous Complete!").update()
    }

    private suspend fun none() = with(bot) {
        move(Angle.Forward, (54 + 10).cm, s)
        move(Angle.Left, (22).cm, s)

        wobble.down()
        wobble.release()
        wobble.up()

        move(Angle.Right, (56 - 40 - 5 + 5 - 5 + 6).cm, s)
        align()
        wobble.down()
        accel(Angle.Backward, (151 + 7).cm, k)

        align()
        wobble.lock()

        accel(Angle.Forward, (140 + 3 + 7).cm, f)
        align()
        move(Angle.Left, (56 - 38 + 15 - 5 - 5).cm, s)


        wobble.release()
        wobble.up()

        move(Angle.Right, 20.cm, s)
        move(Angle.Backward, 20.cm, f)
    }

    private suspend fun partial() = with(bot) {
        intake.intake()

        move(Angle.Backward, 20.cm, hardware.m1.velocity * 0.4)
        move(Angle.Backward, 30.cm, hardware.m1.velocity * 0.2)


        shooter.revup()

        move(Angle.Forward, 49.cm, s)
        align()

//        intake.stop()

        align()
        shooter.shoot(3)
        shooter.revdown()


        //

//        move(Angle.Left, 20.cm, f)

        accel(Angle.Forward, 111.cm, f)
        move(Angle.Right, 30.cm, k)

        align()
        wobble.down()
        wobble.release()
        wobble.up()

        // go to line

        move(Angle.Backward, 90.cm, q)

//        align()
//        accel(Angle.Backward, (220 - 9 + 5 - 7 + 3 + 5 + 5 - 10 - 5 - 5 + 2).cm, k)
//        align()
//
//        wobble.downAsync()                          // ----+
//                                                    //     |
//        move(Angle.Left, (23 + 15 + 8 + 4 - 3 + 3 + 10 - 20).cm, s)   //     |
//        // align()                                     //     |
//                                                    //     |
//        hardware.wobble.stop()                      // ----+
//
//        wobble.lock()
//
//        move(Angle.Right, (38 + 4 - 3 + 10 + 10 - 20 + 5).cm, s)
//        align()
//        accel(Angle.Forward, (200 - 9 + 5 - 7 + 3 + 5 - 5 - 5 + 2).cm, q)
//
//        align()
//        wobble.release()
//        wobble.up()
//
//        move(Angle.Backward, 72.cm, f)
    }

    private suspend fun full() = with(bot) {
        move(Angle.Right, 7.cm, s)

        // first 2
        intake.intake()

        move(Angle.Backward, 20.cm, hardware.m1.velocity * 0.6)
        move(Angle.Backward, 25.cm, hardware.m1.velocity * 0.2)


        shooter.revup()

        move(Angle.Forward, (44 + 10 - 1.7 - 1.7 - 1.7 - 1.7 - 1.7).cm, s)
        align()

//        intake.stop()

        shooter.shoot(3)
        shooter.revdown()

        // last 2

//        intake.intake()

        move(Angle.Backward, (40 + 10 - 1.7 - 1.7 - 1.7 - 1.7 - 1.7).cm, hardware.m1.velocity * 0.6)
        move(Angle.Backward, 25.cm, hardware.m1.velocity * 0.2)

        shooter.revup()

        move(Angle.Forward, (65 + 10 - 1.7 - 1.7 - 1.7 - 1.7 - 1.7).cm, f)
        align()

        wobble.downAsync()

        shooter.shoot(3)
        shooter.revdown()

        intake.stop()

        hardware.wobble.stop()

        //

        move(Angle.Left, 10.cm, f)

        accel(Angle.Forward, (150 - 10).cm, q)
        move(Angle.Forward, q); Thread.sleep(400L); robot.stop()

        wobble.release()
        wobble.up()

        move(Angle.Backward, 120.cm, q)
//        move(Angle.Right, (10 + 36 - 27).cm, f)
//        align()

        // START

//        intake.intake()
//
//        accel(Angle.Backward, (150).cm, q)
//        move(Angle.Backward, 15.cm, hardware.m1.velocity * 0.2)
//
//        shooter.revup()
//
//        move(Angle.Forward, 30.cm, s)
//        align()
//
//        intake.stop()
//
//        shooter.shoot(4)
//        shooter.revdown()
//
//        move(Angle.Forward, 15.cm, q)


//        intake.intake()
//
//        wobble.downAsync()                         // ---+
//        accel(Angle.Backward, (242 - 5 + 5).cm, q) //    |
//        hardware.wobble.stop()                     // ---+
//
//        intake.stop()
//
//        align()
//        wobble.lock()
//        move(Angle.Left, 5.cm, k)
//        align()
//
//        accel(Angle.Forward, (250 - 5 + 5).cm, q)
//        move(Angle.Forward, s); Thread.sleep(300L); robot.stop()
//
////        align((-10).deg)
//        rotate(12.deg, q)
//        wobble.release()
//        wobble.up()
//
//        move(Angle.Right, 5.cm, s)
//        move(Angle.Backward, 122.cm, q)
    }
}