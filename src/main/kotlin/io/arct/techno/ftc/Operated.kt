package io.arct.techno.ftc

import io.arct.ftc.eventloop.OperationMode
import io.arct.ftc.eventloop.OperationMode.Type
import io.arct.rl.hardware.input.Controller
import io.arct.rl.hardware.input.DPad
import io.arct.rl.units.*
import io.arct.techno.ftc.jank.LessJankDrive
import io.arct.techno.ftc.util.Bot
import io.arct.techno.ftc.util.bot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@OperationMode.Register(Type.Operated, name = "TeleOp")
class Operated : OperationMode() {
    private val gamepad0 by Controller
    private val gamepad1 by Controller

    val bot: Bot = bot()

    val spotTurnVelocity = -1.0
    val precisionSpotTurnVelocity = -0.3
    val wobbleSpeed = 0.5
    val precisionWobbleSpeed = 0.3

    val jank = LessJankDrive(bot)

    var power = bot.calibration.shooterPower
    var high = bot.calibration.shooterHigh

    var grey = false

    override suspend fun loop() = with(bot) {
        var stick = false
        gamepad0 {
            active {
                jank.apply(gamepad0)
            }

            active {
                if (+gamepad0.a)
                    stick = true
            }

            active(Controller::lt, .0) {
                rotate(bot.velocity * (gamepad0.lt * spotTurnVelocity))
            }

            active(Controller::rt, .0) {
                rotate(bot.velocity * (gamepad0.rt * -spotTurnVelocity))
            }

            active(Controller::lb) {
                rotate(bot.velocity * precisionSpotTurnVelocity)
            }

            active(Controller::rb) {
                rotate(bot.velocity * -precisionSpotTurnVelocity)
            }

            click(Controller::y) {
                ralign(-(5.5).deg)
            }

            click(Controller::b) {
                ralign(-(11 - 5.5).deg)
            }

            click(DPad::up) { high += 0.01 }
            click(DPad::down) { high -= 0.01 }
            click(DPad::left) { power -= 0.01 }
            click(DPad::right) { power += 0.01 }
        }

        gamepad1 {
            active {
                hardware.wobble.power(gamepad1.left.y * wobbleSpeed + gamepad1.right.y * precisionWobbleSpeed)
            }

            active {
                if (+gamepad1.y) bot.wobble.releaseAsync() else bot.wobble.lockAsync()
                hardware.variableShooter.position = if (+gamepad1.x) power else high
            }

            active {
                when {
                    gamepad1.lt >= 0.5 -> intake.outtake(0.6)
                    +gamepad1.lb ->       intake.intake()
                    else ->               intake.stop()
                }
            }

            active {
                if (+gamepad1.b)
                    stick = true
            }

            active {
                if (gamepad1.rt >= 0.5)
                    shooter.revup()
                else
                    shooter.revdown()
            }

            click(Controller::rb) {
                if (!grey) GlobalScope.async {
                    grey = true
                    while (+gamepad1.rb)
                        shooter.cshoot()
                    grey = false
                }
            }

            click(DPad::up) { high += 0.01 }
            click(DPad::down) { high -= 0.01 }
            click(DPad::left) { power -= 0.01 }
            click(DPad::right) { power += 0.01 }
        }

        bot.stick(stick)
    }
}