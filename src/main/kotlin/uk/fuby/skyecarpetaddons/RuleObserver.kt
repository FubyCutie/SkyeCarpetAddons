package uk.fuby.skyecarpetaddons

import carpet.api.settings.CarpetRule
import carpet.api.settings.SettingsManager
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.block.Block
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import uk.fuby.skyecarpetaddons.SkyeCarpetAddons.logger

object RuleObserver : SettingsManager.RuleObserver {
	override fun ruleChanged(source: ServerCommandSource?, changedRule: CarpetRule<*>, userInput: String) {

		when (changedRule.name()) {
			Options::fastMinecarts.name -> fastMinecartParser(userInput)
		}
	}

	private fun fastMinecartParser(userInput: String) {
		if (userInput == "off") return
		val blockMap = mutableMapOf<Block, Int>()
		for (values in userInput.split(", ")
			.toTypedArray()) {
			val blockBehaviours = values.split(" ")
			try {
				val result = BlockArgumentParser.blockOrTag(
					Registries.BLOCK.readOnlyWrapper,
					blockBehaviours[0], false
				)
				val multiplier = blockBehaviours[1].toInt()
				result.ifLeft {
					val currentMultiplier = blockMap[it.blockState.block]
					if (currentMultiplier == null || currentMultiplier < multiplier) {
						blockMap[it.blockState.block] = multiplier
					}
				}
				result.ifRight {tagResult ->
					tagResult.tag.forEach {
						val currentMultiplier = blockMap[it.value()]
						if (currentMultiplier == null || currentMultiplier < multiplier) {
							blockMap[it.value()] = multiplier
						}
					}
				}
			} catch (e: CommandSyntaxException) {
				logger.error("Failed to parse minecart speedup")
			}
		}
		Options.parsedFastMinecartBehaviour = blockMap
	}
}