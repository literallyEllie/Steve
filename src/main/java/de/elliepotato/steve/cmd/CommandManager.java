package de.elliepotato.steve.cmd;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.cmd.model.CustomCommand;
import de.elliepotato.steve.module.DataHolder;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

/**
 * @author Ellie for VentureNode LLC
 * at 03/02/2018
 */
public class CommandManager extends ListenerAdapter implements DataHolder {

    private Steve bot;

    private Map<String, Command> commandMap;

    private Set<Long> stopMessagingMeMemory;

    /**
     * Manager to handle commands and borrowed methods from other Discord projects.
     * @param ventureBot The bot instance.
     */
    public CommandManager(Steve ventureBot) {
        this.bot = ventureBot;
        this.commandMap = Maps.newHashMap();
        this.stopMessagingMeMemory = Sets.newHashSet();

        new Reflections("de.elliepotato.steve.cmd.commands").getSubTypesOf(Command.class).forEach(aClass -> {
            try {
                if (aClass != CustomCommand.class) {
                    Command command = (Command) aClass.getConstructors()[0].newInstance(bot);
                    commandMap.put(command.getLabel().toLowerCase(), command);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                bot.getLogger().warn("Failed to load command " + aClass.getSimpleName() + "!", e);
                e.printStackTrace();
            }

        });

        bot.getLogger().info("Loaded " + commandMap.size() + " commands!");
    }

    @Override
    public void shutdown() {
        commandMap.clear();
        stopMessagingMeMemory.clear();
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        User author = event.getAuthor();
        if ((author.getIdLong() == Constants.PRESUMED_SELF.getIdLong()) ||
                stopMessagingMeMemory.contains(author.getIdLong())) return;

        stopMessagingMeMemory.add(author.getIdLong());
        event.getChannel().sendMessage("Messages here are not replied to, if you require help please open a ticket.").queue(); // keep it generic.
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        final Message message = event.getMessage();
        final String msg = message.getContentRaw();

        if (!msg.startsWith(bot.getConfig().getCommandPrefix())
                || msg.length() <= bot.getConfig().getCommandPrefix().length()) return; // block out unrelated messages or just "!" messages

        final Guild guild = event.getGuild();

        if (guild.getIdLong() != Constants.GUILD_BISECT.getIdLong()
                && guild.getIdLong() != Constants.GUILD_MELON.getIdLong()
                && guild.getIdLong() != Constants.GUILD_DEV.getIdLong()) return; // this should never be true but w/e


        final Member member = event.getMember();
        if (member.getUser().getIdLong() == Constants.PRESUMED_SELF.getIdLong() || member.getUser().isBot()) return; // dont reply to self or bots.

        final String[] argsWLabel = msg.substring(bot.getConfig().getCommandPrefix().length()).split(" ");

        final Command command = getCommand(argsWLabel[0], true);
        if (command == null) {
            // try for custom commands

            final CustomCommand customCommand = getBot().getCustomCommandManager().getCustomCommandsOf(event.getGuild().getIdLong()).get(argsWLabel[0].toLowerCase());

            if (customCommand != null) {
                String[] argsNoLabel = null;
                // rip
                if (argsWLabel.length > 1) {
                    argsNoLabel = new String[argsWLabel.length - 1];
                    System.arraycopy(argsWLabel, 1, argsNoLabel, 0, argsNoLabel.length);
                }

                customCommand.execute(new CommandEnvironment(customCommand, member, event.getChannel(), message, (argsNoLabel == null ? new String[0] : argsNoLabel)));
            }

            return;
        }

        String[] argsNoLabel = null;
        if (argsWLabel.length > 1) {
            argsNoLabel = new String[argsWLabel.length - 1];
            System.arraycopy(argsWLabel, 1, argsNoLabel, 0, argsNoLabel.length);
        }

        command.execute(new CommandEnvironment(command, member, event.getChannel(), message, (argsNoLabel == null ? new String[0] : argsNoLabel)));
    }

    /**
     * @return The bot instance
     */
    public Steve getBot() {
        return bot;
    }

    /**
     * @return The command map of active commands.
     */
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    /**
     * Command getter
     * @param label The command label/input string
     * @param alias Should it check aliases of commands too
     * @return A {@link Command} if found, else: null.
     */
    public Command getCommand(String label, boolean alias) {
        if (commandMap.containsKey(label.toLowerCase()))
            return commandMap.get(label.toLowerCase());

        if (alias) {
            for (Command command : commandMap.values()) {
                if (command.getAliases().contains(label.toLowerCase()))
                    return command;
            }
        }

        return null;
    }
}
