package de.elliepotato.steve.cmd.model

import com.google.common.base.Joiner
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

/**
 * @author Ellie for VentureNode LLC
 * at 03/02/2018
 */
class CommandEnvironment(val cmd: Command, val sender: Member, val channel: TextChannel, val message: Message, val args: Array<String>) {

    override fun toString(): String = "${sender.user.idLong} (${sender.effectiveName}) executed command ${cmd.label} with parameters of " +
            Joiner.on(", ").join(args)

}