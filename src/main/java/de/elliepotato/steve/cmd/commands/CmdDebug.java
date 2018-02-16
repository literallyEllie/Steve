package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.core.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 15/02/2018
 */
public class CmdDebug extends Command {

    public CmdDebug(Steve steve) {
        super(steve, "debug", "Steve's debug system", Lists.newArrayList(), Permission.MESSAGE_MANAGE, Lists.newArrayList());
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        getBot().getDebugger().toggle(environment.getChannel());
    }

}
