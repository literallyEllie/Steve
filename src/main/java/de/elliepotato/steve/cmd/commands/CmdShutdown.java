package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.core.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 04/02/2018
 */
public class CmdShutdown extends Command {

    /**
     * Command to shut down the bot, this only requires a permission as it is meant to be accessible
     * by server admins if a problem persists.
     * Also used to develop without having to shutdown from terminal.
     *
     * @param steve The bot instance.
     */
    public CmdShutdown(Steve steve) {
        super(steve, "shutdown", "Shutdown the bot", Lists.newArrayList(), Permission.KICK_MEMBERS,
                Lists.newArrayList());
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        getBot().shutdown();
    }

}
