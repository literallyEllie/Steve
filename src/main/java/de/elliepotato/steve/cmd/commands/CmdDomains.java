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
     * This allows for whitelisting and blacklisting domains.
     * <p>
     * The blacklisting of domains is primarily for use in the server-advertisements as the rest runs on a whitelist.
     *
     * @param steve The bot instance
     */
    public CmdDomains(Steve steve) {
        super(steve, "domains", "Domain whitelist management", Lists.newArrayList(), Permission.MESSAGE_MANAGE,
                Lists.newArrayList("<whitelist | blacklist> <domain.com>"));
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final String[] args = environment.getArgs();

        switch (args[0].toLowerCase()) {
            case "whitelist":
                MessageChecker messageChecker = getBot().getMessageChecker();

                String stripped = args[1].toLowerCase().replaceAll("http?s/{2}", "").replace("/", "").trim();

                if (messageChecker.getAllowedDomains().contains(stripped)) {
                    getBot().tempMessage(channel, "This domain is already whitelisted.", 7, null);
                    return;
                }

                messageChecker.getAllowedDomains().add(stripped);
                try {
                    messageChecker.getDomainsFile().write(getBot().getMessageChecker().getAllowedDomains());
                    getBot().tempMessage(channel, "Whitelisted the domain `" + stripped + "`.", 7, environment.getMessage());
                } catch (IOException e) {
                    getBot().messageChannel(channel, ":x: Failed to write domains to file!");
                    e.printStackTrace();
                }
                break;
            case "blacklist":
                messageChecker = getBot().getMessageChecker();
                stripped = args[1].toLowerCase().replaceAll("http?s/{2}", "").replace("/", "").trim();

                if (messageChecker.getBlacklistedDomains().contains(stripped)) {
                    getBot().tempMessage(channel, "This domain is already blacklisted.", 7, null);
                    return;
                }

                messageChecker.getBlacklistedDomains().add(stripped);
                try {
                    messageChecker.getBlacklistedFile().write(getBot().getMessageChecker().getBlacklistedDomains());
                    getBot().tempMessage(channel, "Blacklisted the domain `" + stripped + "`.", 7, environment.getMessage());
                } catch (IOException e) {
                    getBot().messageChannel(channel, ":x: Failed to write domains to blacklisted file!");
                    e.printStackTrace();
                }
                break;
            default:
                getBot().messageChannel(environment.getChannel(), correctUsage());
        }

    }


}
