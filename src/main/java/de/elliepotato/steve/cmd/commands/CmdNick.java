package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 17/08/2018
 */
public class CmdNick extends Command {

    public CmdNick(Steve steve) {
        super(steve, "nick", "Forcefully change the nickname of another user", Lists.newArrayList(), Permission.KICK_MEMBERS,
                "u:<user>", "t:<to>", "r:<reason>");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final String[] args = environment.getArgs();

        String current = "";

        User user;
        String newNick;
        String reason;

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("u:")) {
                current = "u";
            } else if (arg.toLowerCase().startsWith("t:")) {
                current = "t";
            } else if (arg.toLowerCase().startsWith("r:")) {
                current = "r";
            }

        }


    }

}
