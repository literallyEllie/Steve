package de.elliepotato.steve.cmd.model

import de.elliepotato.steve.Steve

/**
 * @author Ellie for VentureNode LLC
 * at 04/02/2018
 */
class CustomCommand(bot: Steve, label: String, description: String,
                    val guildId: Long, var responseMessage: String): Command(bot, label, description) {

    override fun abstractExecute(environment: CommandEnvironment) {
        if (environment.channel.guild.idLong != guildId) return
        bot.messageChannel(environment.channel, responseMessage)
    }


}
