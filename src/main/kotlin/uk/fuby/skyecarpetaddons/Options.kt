package uk.fuby.skyecarpetaddons

import carpet.api.settings.CarpetRule
import carpet.api.settings.Rule
import carpet.api.settings.RuleCategory
import carpet.api.settings.Validator
import carpet.utils.Messenger
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource

object Options {

    @JvmField
    @Rule(
        categories = [RuleCategory.FEATURE]
    )
    var thunderRitual = false

    @JvmField
    @Rule(
        categories = [RuleCategory.FEATURE]
    )
    var traderRitual = false

    @JvmField
    @Rule(
        categories = [RuleCategory.FEATURE],
        validators = [FastMinecartsValidator::class],
        options = ["off", "smooth_stone 4, #stone_bricks 2", "minecraft:gravel 2, minecraft:netherrack 4"],
        strict = false
    )
    var fastMinecarts = "off"

    var parsedFastMinecartBehaviour = mapOf<Block, Int>()

    class FastMinecartsValidator : Validator<String>() {
        override fun validate(
            source: ServerCommandSource?,
            changingRule: CarpetRule<String>?,
            newValue: String?,
            userInput: String?
        ): String? {
            if (newValue.isNullOrBlank()) return null;
            if (newValue == "off") return newValue
            newValue.split(", ").forEach {
                val value = it.split(" ")
                try {
                    BlockArgumentParser.blockOrTag(Registries.BLOCK.readOnlyWrapper, value[0], false)
                } catch (e : CommandSyntaxException) {
                    Messenger.m(source, "r Values must be valid blocks or tags e.g. minecraft:oak_planks, or #minecraft:planks")
                    return null
                }

                try {
                    value[1].toInt()
                } catch (e: NumberFormatException) {
                    Messenger.m(source, "r Speed multipliers must be an integer value")
                    return null
                } catch (e: IndexOutOfBoundsException) {
                    Messenger.m(source, "r Must include a speed multiplier")
                    return null
                }
            }
            return newValue
        }
    }

    @JvmField
    @Rule(
        categories = [RuleCategory.CREATIVE],
        validators = [ItemRngValidator::class],
        options = ["false", "0.0 0.0 0.0", "-0.1 -0.1 -0.1", "0.1 0.1 0.1"],
        strict = false
    )
    var hardcodedItemRNG = "false"

    class ItemRngValidator : Validator<String>() {
        override fun validate(
            source: ServerCommandSource?,
            changingRule: CarpetRule<String>?,
            newValue: String?,
            userInput: String?
        ) : String? {
            newValue ?: return null
            if (newValue == "false") {
                return newValue
            }

            newValue.split(" ").forEach {
                try {
                    if (it.toDouble() !in -0.1..0.1) {
                        Messenger.m(source, "r Values must be in range -0.1 to 0.1")
                        return null
                    }
                } catch (_: NumberFormatException) {
                    Messenger.m(source, "r Invalid numbers, must be in format 123.456")
                    return null
                }
            }
            return newValue
        }
    }

    @JvmField
    @Rule(
        categories = [RuleCategory.CREATIVE],
        validators = [DispenserItemRngValidator::class],
        options = ["false", "0.0 0.0 0.0", "-0.2033650 -0.1033650 -0.1033650", "0.2033650 0.1033650 0.1033650"],
        strict = false
    )
    var hardcodedDispenserItemRNG = "false"

    class DispenserItemRngValidator : Validator<String>() {
        override fun validate(
            source: ServerCommandSource?,
            changingRule: CarpetRule<String>?,
            newValue: String?,
            userInput: String?
        ) : String? {
            newValue ?: return null
            if (newValue == "false") {
                return newValue
            }

            val values = newValue.split(" ")

            for ((index, value) in values.withIndex()) {
                if (index == 0) {
                    try {
                        if (value.toDouble() !in -0.2033650..0.2033650) {
                            Messenger.m(source, "r First value must be in range -0.2033650 to 0.2033650")
                            return null
                        }
                    } catch (_: NumberFormatException) {
                        Messenger.m(source, "r Invalid numbers, must be in format 123.456")
                        return null
                    }
                }
                else {
                    try {
                        if (value.toDouble() !in -0.1033650..0.1033650) {
                            Messenger.m(source, "r Second and third value must be in range -0.1033650 to 0.1033650")
                            return null
                        }
                    } catch (_: NumberFormatException) {
                        Messenger.m(source, "r Invalid numbers, must be in format 123.456")
                        return null
                    }
                }
            }
            return newValue
        }
    }

    @JvmField
    @Rule(
        categories = [RuleCategory.FEATURE],
    )
    var movableReinforcedDeepslate = false
}