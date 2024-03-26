package uk.fuby.skyecarpetaddons

import carpet.CarpetExtension
import carpet.CarpetServer
import carpet.api.settings.SettingsManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import java.io.IOException


object SkyeCarpetAddons : ModInitializer, CarpetExtension {
    val logger = LoggerFactory.getLogger("skye-carpet-addons")

	override fun onInitialize() {
		CarpetServer.manageExtension(this)
	}

	override fun version(): String {
		return "Skye-Carpet-Addons"
	}

	override fun onGameStarted() {
		val carpetSettingsManager = CarpetServer.settingsManager
		carpetSettingsManager.parseSettingsClass(Options.javaClass)
		carpetSettingsManager.registerRuleObserver(RuleObserver)
	}

	override fun canHasTranslations(lang: String?): MutableMap<String, String> {
		return getTranslationFromResourcePath(lang)
	}

	private fun getTranslationFromResourcePath(lang: String?): MutableMap<String, String> {
		lang ?: return mutableMapOf()
		val langFile = this.javaClass.classLoader.getResource("assets/skye-carpet-addons/lang/$lang.json")
		langFile ?: return mutableMapOf()
		val jsonData: String = try {
			langFile.readText()
		} catch (e: IOException) {
			return mutableMapOf()
		}
		return Json.decodeFromJsonElement<Map<String, String>>(Json.parseToJsonElement(jsonData)).toMutableMap()
	}
}