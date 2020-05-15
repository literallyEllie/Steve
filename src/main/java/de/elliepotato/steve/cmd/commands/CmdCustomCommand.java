package de.elliepotato.steve.cmd.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.cmd.model.CustomCommand;
import de.elliepotato.steve.util.UtilEmbed;
import de.elliepotato.steve.util.UtilPagination;
import de.elliepotato.steve.util.UtilString;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Ellie for VentureNode LLC
 * at 05/02/2018
 */
public class CmdCustomCommand extends Command {

    private static final int LIST_MAX_PER_PAGE = 10;

    /**
     * A command to easily manage custom commands which are can be utilized by everyone throughout the servers
     * for easy use.
     *
     * @param steve the bot instance
     */
    public CmdCustomCommand(Steve steve) {
        super(steve, "customcommands", "Custom command manager", Lists.newArrayList("cc"),
                Permission.KICK_MEMBERS, "<create | delete | set | list>");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final Member sender = environment.getSender();
        final String[] args = environment.getArgs();
        final TextChannel channel = environment.getChannel();

        switch (args[0].toLowerCase()) {
            case "create":
                // cc create <name> <response>
                if (args.length < getMinArgs() + 2) {
                    environment.reply(correctUsage("create <name> [--g[lobal]] --<description> --<response>"));
                    return;
                }

                String label = args[1].toLowerCase();

                if (getBot().getCommandManager().getCommand(label, true) != null) {
                    environment.replyBadSyntax(sender.getAsMention() + ", the command name `" + label + "` is reserved.");
                    return;
                }

                final Map<String, CustomCommand> customCommandMap = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong());
                if (customCommandMap != null && customCommandMap.containsKey(label)) {
                    environment.replyBadSyntax(sender.getAsMention() + ", the custom command by `" + label + "` already exist on this server.");
                    return;
                }

                final String[] parts = UtilString.getFinalArg(args, 2).split("--");

                boolean global;
                String description;
                String response;

                if (parts.length > 2) {
                    global = parts[1].trim().equalsIgnoreCase("g") || parts[1].trim().equalsIgnoreCase("global");
                    description = parts[global ? 2 : 1].trim();
                    response = parts[global ? 3 : 2].trim();    // bit lazy.
                } else {
                    environment.reply(correctUsage("create <name> [--g[lobal]] --<description> --<response>"));
                    return;
                }

                final CustomCommand customCommand = new CustomCommand(getBot(), label, description, global ? 0 : channel.getGuild().getIdLong(), response);
                getBot().getCustomCommandManager().addCustomCommand(customCommand.getGuildId(), customCommand, true);

                getBot().messageChannel(environment.getChannel(), UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                        .setTitle((global ? "Global " : "") + "Custom command created!")
                        .addField("Label", label, true).addField("Description", customCommand.getDescription(), true)
                        .addField("Response", customCommand.getResponseMessage(), true).build());
                // woop

                break;
            case "delete":
                // "cc delete <name>"
                if (args.length < getMinArgs() + 1) {
                    environment.reply(correctUsage("delete <name>"));
                    return;
                }
                label = args[1].toLowerCase();

                final Map<String, CustomCommand> guildCommands = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong());
                if (!guildCommands.containsKey(label)) {
                    environment.replyBadSyntax(sender.getAsMention() + ", the custom command by `" + label + "` doesn't exist for this server.");
                    return;
                }

                getBot().getCustomCommandManager().deleteCustomCommand(guildCommands.get(label).getGuildId(), label);
                environment.replySuccess("Custom command `" + label + "` deleted!");
                break;
            case "set":
                // "cc set <name> <type> <value>"
                if (args.length < getMinArgs() + 3) {
                    environment.reply(correctUsage("set <name> <" + Joiner.on(" | ").join(CustomCommandValue.values()) + "> <value>"));
                    return;
                }

                label = args[1].toLowerCase();

                CustomCommand command = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong()).get(label);
                if (command == null) {
                    environment.replyBadSyntax(sender.getAsMention() + ", the custom command by `" + label + "` doesn't exist for this server.");
                    return;
                }

                final CustomCommandValue customCommandValue = CustomCommandValue.fromString(args[2]);
                if (customCommandValue == null) {
                    environment.replyBadSyntax(sender.getAsMention() + ", invalid custom command type `" + args[2] + "`.");
                    return;
                }

                final String data = UtilString.getFinalArg(args, 3);
                setValue(channel.getGuild().getIdLong(), command, customCommandValue, data);

                getBot().messageChannel(environment.getChannel(), UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                        .setTitle("Custom command updated!")
                        .addField("Label", label, true).addField("Description", command.getDescription(), true)
                        .addField("Response", command.getResponseMessage(), true).build());

                break;
            case "list":
                int page = 0;
                if (args.length > getMinArgs()) {
                    try {
                        page = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException e) {
                        environment.replyBadSyntax("Invalid page number (" + args[1] + ") " + correctUsage("list [page]"));
                        return;
                    }
                }

                getBot().getDebugger().write("Loading page " + page);

                final Map<String, CustomCommand> guildCommandMap = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong());

                if (guildCommandMap != null) {
                    int maxPages = UtilPagination.getPageCount(guildCommandMap.size(), LIST_MAX_PER_PAGE);
                    if (page >= maxPages) {
                        page = maxPages - 1;
                    }

                    EmbedBuilder embedBuilder = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                            .setTitle("Server Commands (Page " + (page + 1) + "/" + maxPages + ")");

                    final CustomCommand[] commandArray = guildCommandMap.values().toArray(new CustomCommand[0]);
                    final int pageElementIndex = UtilPagination.getPageElementIndex(page, maxPages, LIST_MAX_PER_PAGE);

                    getBot().getDebugger().write("Max pages " + maxPages + " , page elemt" + pageElementIndex);

                    for (int i = pageElementIndex; i < Math.min(commandArray.length, pageElementIndex + LIST_MAX_PER_PAGE); i++) {
                        final CustomCommand cmd = commandArray[i];
                        embedBuilder.addField(cmd.getLabel(), (cmd.getGuildId() == 0L ? "[GLOBAL] " : "") + cmd.getDescription(), false);
                    }

                    environment.reply(embedBuilder.build());
                } else {
                    environment.reply("There are no custom commands for this server yet.");
                    return;
                }

                break;
            default:
                environment.reply(correctUsage());
        }

    }

    private void setValue(long guildId, CustomCommand customCommand, CustomCommandValue type, String value) {

        switch (type) {
            case DESCRIPTION:
                customCommand.setDescription(value);
                break;
            case RESPONSE:
                customCommand.setResponseMessage(value);
                break;
            default:
                return; // ???
        }

        getBot().getCustomCommandManager().addCustomCommand(guildId, customCommand, true);
    }

    enum CustomCommandValue {

        DESCRIPTION,
        RESPONSE;

        public static CustomCommandValue fromString(String input) {
            for (CustomCommandValue customCommandValue : values()) {
                if (customCommandValue.name().equalsIgnoreCase(input))
                    return customCommandValue;
            }
            return null;
        }

    }

}
