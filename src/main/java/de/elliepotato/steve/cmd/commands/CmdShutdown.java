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

    public CmdShutdown(Steve steve) {
        super(steve, "shutdown", "Shutdown the bot", Lists.newArrayList(), Permission.KICK_MEMBERS,
                Lists.newArrayList());
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        getBot().shutdown();
    }

}
