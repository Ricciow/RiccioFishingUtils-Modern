package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.constants.skyblock.SkillType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.SkillTracker
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object SkillsDebug : AbstractCommand("skills") {
    override val description: String = "View tracked skill XP and levels."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder
            .executes { context ->
                context.source.sendFeedback(TextUtils.rfuLiteral("--- Skills Debug ---", TextStyle(TextColor.GOLD)))
                for (skill in SkillType.entries) {
                    val xp = SkillTracker.getSkillXp(skill)
                    val level = SkillTracker.getSkillLevel(skill)
                    val currentLevelXp = SkillTracker.xpRequiredForLevel(level)
                    val nextLevelXp = SkillTracker.xpRequiredForLevel(level + 1)
                    val xpInLevel = xp - currentLevelXp
                    val xpNeededForNext = nextLevelXp - currentLevelXp
                    context.source.sendFeedback(
                        Component.literal("${TextColor.YELLOW}${skill.displayName}: ${TextColor.LIGHT_GREEN}${xp} XP (Level ${level}) ${TextColor.GRAY}[${xpInLevel}/${xpNeededForNext}]")
                    )
                }
                1
            }
            .then(
                arg("skill", StringListArgumentType(SkillType.entries.map { it.displayName }))
                    .executes { context ->
                        val skillName = StringArgumentType.getString(context, "skill")
                        val skill = SkillType.fromName(skillName)
                        if (skill == null) {
                            context.source.sendFeedback(
                                TextUtils.rfuLiteral("Unknown skill: $skillName", TextStyle(TextColor.RED))
                            )
                            return@executes 1
                        }

                        val xp = SkillTracker.getSkillXp(skill)
                        val level = SkillTracker.getSkillLevel(skill)
                        val currentLevelXp = SkillTracker.xpRequiredForLevel(level)
                        val nextLevelXp = SkillTracker.xpRequiredForLevel(level + 1)
                        val xpInLevel = xp - currentLevelXp
                        val xpNeededForNext = nextLevelXp - currentLevelXp
                        context.source.sendFeedback(
                            TextUtils.rfuLiteral("--- ${skill.displayName} Debug ---", TextStyle(TextColor.GOLD))
                        )
                        context.source.sendFeedback(
                            Component.literal("${TextColor.YELLOW}Total XP: ${TextColor.LIGHT_GREEN}${xp}")
                        )
                        context.source.sendFeedback(
                            Component.literal("${TextColor.YELLOW}Level (with overflow): ${TextColor.LIGHT_GREEN}${level}")
                        )
                        context.source.sendFeedback(
                            Component.literal("${TextColor.YELLOW}Progress: ${TextColor.LIGHT_GREEN}${xpInLevel}/${xpNeededForNext}")
                        )
                        1
                    }
            )
    }
}
