package de.elliepotato.steve.cmd

import de.elliepotato.steve.Steve

/**
 * @author Ellie for VentureNode LLC
 * at 04/02/2018
 */
class CustomCommand(bot: Steve, label: String, description: String,
                    private val responseMessage: String): Command(bot, label, description) {

    override fun abstractExecute(environment: CommandEnvironment) =
            bot.messageChannel(environment.channel, responseMessage)

}
