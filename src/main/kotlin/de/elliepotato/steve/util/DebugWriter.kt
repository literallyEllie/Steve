package de.elliepotato.steve.util

import de.elliepotato.steve.Steve
import net.dv8tion.jda.core.entities.Channel
import net.dv8tion.jda.core.entities.TextChannel

/**
 * @author Ellie for VentureNode LLC
 * at 15/02/2018
 */
class DebugWriter(private val steve: Steve) {

    var enabled: Boolean = false
    private lateinit var outputChannel: Channel

    fun toggle(outputChannel: Channel) {
        this.enabled = !this.enabled
        this.outputChannel = outputChannel
        steve.messageChannel(outputChannel, "[DEBUG] Now ${if (enabled) "enabled" else "disabled"} debug mode.")
    }

    fun write(message: String) {
        if (!enabled) return
        steve.messageChannel(outputChannel, "[DEBUG] $message")
        steve.logger.debug(message)
    }

}
