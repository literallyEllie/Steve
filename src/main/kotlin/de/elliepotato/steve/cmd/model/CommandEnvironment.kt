package de.elliepotato.steve.cmd.model

import com.google.common.base.Joiner
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * @author Ellie for VentureNode LLC
 * at 03/02/2018
 */
class CommandEnvironment(val cmd: Command, val sender: Member, val channel: TextChannel, val message: Message, val args: Array<String>) {

    /**
     * Reply with a message to the channel
     */
    fun reply(message: String) {
        channel.sendMessage(message).queue()
    }

    /**
     * Reply with a message embed to the channel
     */
    fun reply(embed: MessageEmbed) {
        channel.sendMessage(embed).queue()
    }

    /**
     * Send a temporary message as a response
     *
     * @param message    The message content
     * @param expireTime After what delay should the message be deleted
     */
    fun replyTemp(message: String, expireTime: Int, cleanupMsg: Message?) {
        channel.sendMessage(message).queue { message1: Message ->
            try {
                message1.delete().queueAfter(expireTime.toLong(), TimeUnit.SECONDS)
            } catch (ignored: ErrorResponseException) {
                // if message deletes before we get to it!
            }
            cleanupMsg?.delete()?.queueAfter(expireTime.toLong(), TimeUnit.SECONDS)
        }
    }

    fun replyBadSyntax(message: String) {
        replyTemp(":x: $message", 10, this.message)
    }

    fun replySuccess(message: String) {
        reply(":thumbsup: $message")
    }

    override fun toString(): String = "${sender.user.idLong} (${sender.effectiveName}) executed command ${cmd.label} with parameters of " +
            Joiner.on(", ").join(args)


}