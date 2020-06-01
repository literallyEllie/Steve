package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageChecker;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Ellie for VentureNode LLC
 * at 16/03/2018
 */
public class CmdDomains extends Command {

    /**
     * Command for domain filter management.
     * This allows for whitelisting domains.
     *
     * @param steve The bot instance
     */
    public CmdDomains(Steve steve) {
        super(steve, "whitelist", "Domain whitelist management", Lists.newArrayList("domain", "domains"), Permission.MESSAGE_MANAGE,
                "<domain.com>");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final String[] args = environment.getArgs();

        switch (args[0].toLowerCase()) {
            case "whitelist":
                environment.replyBadSyntax("This command is updated, " + correctUsage());
                break;
            case "blacklist":
                environment.replyBadSyntax("Blacklisted domains are no longer needed.");
                break;
            default:
                MessageChecker messageChecker = getBot().getMessageChecker();

                String stripped = args[0].toLowerCase().replaceAll("http?s/{2}", "").replace("/", "").trim();

                if (messageChecker.getAllowedDomains().contains(stripped)) {
                    environment.replyBadSyntax("This domain is already whitelisted.");
                    return;
                }

                messageChecker.getAllowedDomains().add(stripped);
                try {
                    messageChecker.getDomainsFile().write(getBot().getMessageChecker().getAllowedDomains());
                    environment.replySuccess("Whitelisted the domain `" + stripped + "`.");
                } catch (IOException e) {
                    environment.replyBadSyntax(":x: Failed to write domains to file!");
                    e.printStackTrace();
                }
        }

    }


}
