package de.elliepotato.steve.cmd.model

import com.google.common.base.Joiner
import com.google.common.collect.Lists
import de.elliepotato.steve.Steve
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.internal.utils.PermissionUtil

/**
 * @author Ellie for VentureNode LLC
 * at 03/02/2018
 */
abstract class Command(val bot: Steve, val label: String, var description: String, val aliases: List<String> = Lists.newArrayList(),
                       val permission: Permission = Permission.MESSAGE_WRITE, vararg val usage: String) {

    val minArgs: Int

    init {
        minArgs = usage.asList().stream().filter { s -> s.contains("<") }.count().toInt()
    }

    /**
     * The method called to carry out the command purpose after "execute(CommandEnvironment)"
     * @param environment The command parameters based on the environment.
     */
    protected abstract fun abstractExecute(environment: CommandEnvironment)

    /**
     * Method to check preconditions and error handling during and before command execution.
     * @param environment The command parameters based on the environment.
     */
    fun execute(environment: CommandEnvironment) {
        val sender: Member = environment.sender

        if (!PermissionUtil.checkPermission(environment.channel, sender, permission)) {
            return bot.tempMessage(environment.channel, ":thumbsdown: I cannot allow you to do that ${sender.asMention}.", 10, environment.message)
        }

        if (environment.args.size < minArgs) return bot.messageChannel(environment.channel, correctUsage())

        try {
            bot.logger.info(environment.toString())
            abstractExecute(environment)
        } catch (ex: Throwable) {
            bot.messageChannel(environment.channel, ":x: Error whilst executing that command (${ex.cause}: ${ex.message})")
            bot.logger.error("Failed to execute command $label!")
            ex.printStackTrace()
        }

    }

    /**
     * A method to simply return the usage of the command, to be posted.
     */
    @JvmOverloads
    fun correctUsage(moreStuff: String = "") = ":thumbsup: Correct usage: `${bot.config.commandPrefix}$label " +
            "${if (moreStuff.isEmpty()) Joiner.on(" ").join(usage) else moreStuff}` **-** $description."

}