package de.elliepotato.steve;

import de.elliepotato.steve.antispam.MessageChecker;
import de.elliepotato.steve.cmd.CommandManager;
import de.elliepotato.steve.cmd.CustomCommandManager;
import de.elliepotato.steve.config.JSONConfig;
import de.elliepotato.steve.console.SteveConsole;
import de.elliepotato.steve.mysql.MySQLManager;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.DebugWriter;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Ellie for VentureNode LLC
 * at 03/02/2018
 */
public class Steve {

    public static final String VERSION = "1.2-RELEASE";
    public static final String[] AUTHORS = {"Ellie#0006"};

    private final Logger LOGGER = LoggerFactory.getLogger("Steve");
    private final DebugWriter DEBUG = new DebugWriter(this);

    private final Pattern PATTERN_USER = Pattern.compile("<@!?([0-9]+)>");

    private final File FILE_CONFIG = new File("config.json");
    private JSONConfig config;

    private JDA jda;

    private CommandManager commandManager;
    private MySQLManager sqlManager;
    private CustomCommandManager customCommandManager;

    private MessageChecker messageChecker;

    private SteveConsole steveConsole;

    /**
     * A project for the hosting companies MelonCube and BisectHosting.
     * Since they both share the same parent company (VentureNode LLC), it was thought
     * a good name for the bot was to be Steve, as suggested by the CEO.
     * However this name wasn't available, so it was chosen for Steve-0 instead.
     * <p>
     * MelonCube: https://www.meloncube.net/
     * BisectHosting: https://www.bisecthosting.com/
     * <p>
     * The project is a joint Java-Kotlin project, most object models due to the nature of Kotlin
     * making it more efficient to do.
     * <p>
     * This bot has no official affiliation with the the company and was simply created
     * in the light of volunteering to help improve the community and ease moderation.
     * <p>
     * The bot will contain:
     * - Custom commands;
     * - Auto-moderation (i.e Spam detection);
     * - Knowledge-Base quick searcher;
     * - <i>More to be decided...</i>
     * <p>
     * <p>
     * Created to help :)
     */
    Steve() {
        Thread.currentThread().setName("Steve-Main");
        init();
    }

    /**
     * Method to prepare the bot and start it.
     */
    private void init() {
        final long start = System.currentTimeMillis();

        // Get config values
        config = new JSONConfig();
        try {
            if (!FILE_CONFIG.exists()) {
                config.create(FILE_CONFIG);
                LOGGER.warn(FILE_CONFIG.getName() + " HAS BEEN CREATED IN THE PROGRAM PATH, PLEASE FILL IT IN BEFORE RESTARTING");
                return;
            }

            config = config.load(FILE_CONFIG);

            try {
                config.validate();
            } catch (NullPointerException | IllegalArgumentException e) {
                LOGGER.error("A config value is " + (e instanceof NullPointerException ? "null" : "invalid") + "!", e);
                e.printStackTrace();
                return;
            }

        } catch (IOException e) {
            LOGGER.error("Failed to setup config! Please check the environment", e);
            e.printStackTrace();
            return;
        }

        // Listener registration
        this.commandManager = new CommandManager(this);
        this.messageChecker = new MessageChecker(this);

        // Setup JDA (blocking).
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(config.getBotToken())
                    .setGame(Game.of(Game.GameType.valueOf(config.getGameType().toUpperCase()), config.getGameOf())) // we already know its valid.
                    .setStatus(OnlineStatus.fromKey(config.getBotStatus().toLowerCase()))
                    .addEventListener(messageChecker, commandManager)
                    .buildBlocking();
        } catch (LoginException | InterruptedException e) {
            LOGGER.error("Failed to setup JDA", e);
            e.printStackTrace();
            return;
        }

        this.sqlManager = new MySQLManager(this);
        this.customCommandManager = new CustomCommandManager(this);

        LOGGER.info("Steve startup completed in " + (System.currentTimeMillis() - start) + "ms. Console thread starting.");
        this.steveConsole = new SteveConsole(this);
        steveConsole.run();

    }

    /**
     * Shut down the bot safely then exits the program.
     *
     * @param exitCode What the program's exit code will be.
     *                 A Java "optional variable", if no input, it will be 0
     *                 Else, will be the first entry to the array.
     */
    public void shutdown(int... exitCode) {

        if (messageChecker != null)
            messageChecker.shutdown();

        if (commandManager != null)
            commandManager.shutdown();

        if (customCommandManager != null)
            customCommandManager.shutdown();

        if (sqlManager != null)
            sqlManager.shutdown();

        if (jda != null)
            jda.shutdown();

        if (steveConsole != null) {
            try {
                steveConsole.join();
            } catch (InterruptedException e) {
                LOGGER.error("Failed to terminate console thread!", e);
                e.printStackTrace();
            }
        }

        LOGGER.info("Bye bye!");
        System.exit((exitCode.length == 0 ? 0 : exitCode[0]));
    }

    /**
     * @return The logger.
     */
    public Logger getLogger() {
        return LOGGER;
    }

    /**
     * @return The debugger out-putter.
     */
    public DebugWriter getDebugger() {
        return DEBUG;
    }

    /**
     * @return The regex pattern to check for a user.
     */
    public Pattern getPatternUser() {
        return PATTERN_USER;
    }

    /**
     * @return Get the config of the session.
     */
    public JSONConfig getConfig() {
        return config;
    }

    /**
     * @return Discord wrapper.
     */
    public JDA getJda() {
        return jda;
    }

    /**
     * @return The command manager.
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * @return The custom command manager.
     */
    public CustomCommandManager getCustomCommandManager() {
        return customCommandManager;
    }

    /**
     * @return The SQL manager.
     */
    public MySQLManager getSqlManager() {
        return sqlManager;
    }

    /**
     * @return The message checker.
     */
    public MessageChecker getMessageChecker() {
        return messageChecker;
    }

    /**
     * Message a discord channel
     *
     * @param channel Channel ID
     * @param message Message to send
     */
    public void messageChannel(long channel, String message) {
        jda.getTextChannelById(channel).sendMessage(message).queue();
    }

    /**
     * Message any channel that implements {@link Channel}
     *
     * @param channel Channel ID
     * @param message Message to send
     */
    public void messageChannel(Channel channel, String message) {
        messageChannel(channel.getIdLong(), message);
    }

    /**
     * Message a discord channel
     *
     * @param channel Channel ID
     * @param message embed to send
     */
    public void messageChannel(long channel, MessageEmbed message) {
        if (jda.getTextChannelById(channel) != null) {
            jda.getTextChannelById(channel).sendMessage(message).queue();
        }
    }

    /**
     * Message any channel that implements {@link Channel}
     *
     * @param channel Channel ID
     * @param message embed to send
     */
    public void messageChannel(Channel channel, MessageEmbed message) {
        messageChannel(channel.getIdLong(), message);
    }

    /**
     * Send a temporary message to a discord channel
     *
     * @param channel    The channel to send to
     * @param message    The message content
     * @param expireTime After what delay should the message be deleted
     */
    public void tempMessage(long channel, String message, int expireTime, Message cleanupMsg) {
        jda.getTextChannelById(channel).sendMessage(message).queue(message1 -> {
            try {
                message1.delete().queueAfter(expireTime, TimeUnit.SECONDS);
            } catch (ErrorResponseException ignored) {
                // if message deletes before we get to it!
            }
            if (cleanupMsg != null) cleanupMsg.delete().queueAfter(expireTime, TimeUnit.SECONDS);
        });
    }

    /**
     * Send a temporary message to a discord channel that implements {@link Channel} (aka all of them)
     *
     * @param channel    The channel to send to
     * @param message    The message content
     * @param expireTime After what delay should the message be deleted
     */
    public void tempMessage(Channel channel, String message, int expireTime, Message cleanupMsg) {
        tempMessage(channel.getIdLong(), message, expireTime, cleanupMsg);
    }

    /**
     * Send a private message to a user
     *
     * @param user    User to message
     * @param content the message content
     */
    public void privateMessage(User user, String content) {
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(content).queue());
    }

    /**
     * Send a private message to a member
     *
     * @param member  Member to message
     * @param content the message content
     */
    public void privateMessage(Member member, String content) {
        privateMessage(member.getUser(), content);
    }

    /**
     * Log a moderation action performed by the bot.
     *
     * @param guild        The guild to mod log to.
     * @param embedBuilder The embed to log
     */
    public void modLog(Guild guild, EmbedBuilder embedBuilder) {
        messageChannel((guild.getIdLong() == Constants.GUILD_BISECT.getIdLong() ? Constants.CHAT_BISECT_MOD.getIdLong() : Constants.CHAT_MELON_MOD.getIdLong()), embedBuilder.build());
    }

    /**
     * Get the default embed builder.
     *
     * @param discordColor The color for the side bit to be
     * @return A embed builder set with a timestamp, color of choose
     * and footer of "AssimilationMC Development Team" and a lovely cake picture.
     */
    public EmbedBuilder getEmbedBuilder(DiscordColor discordColor) {
        return new EmbedBuilder()
                .setColor(discordColor.color)
                .setTimestamp(Instant.now())
                .setFooter("Just Steve-0 doing his job.", null);
    }

    /**
     * Attempt to parse from an input string to a {@link User}.
     *  Will attempt to parse from: a raw ID, a mention or a User#Discrim
     * @param input The input to parse from.
     * @return the user or null if failed to parse
     */
    public User parseUser(String input) {

        // raw id
        long id;
        try {
            id = Long.parseLong(input);
            return jda.getUserById(id);
        } catch (NumberFormatException e) {
        }

        // a mention
        if (PATTERN_USER.matcher(input).matches()) {
            try {
                id = Long.parseLong((input.replace("<", "")
                        .replace(">", "").replace("@", "")
                        .replace("!", ""))); // idk
                return jda.getUserById(id);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // accept user#discrim
        if (input.contains("#")) {

            final String[] parts = input.split("#");
            final String name = parts[0];
            final String discrim = parts[1]; // not parsing to int cus 0006 == 6

            for (final User user : jda.getUsersByName(name, true)) {
                if (user.getName().equalsIgnoreCase(name) && user.getDiscriminator().equals(discrim)) {
                    return user;
                }
            }

        }

        return null;
    }

    public enum DiscordColor {

        KICK(new Color(232, 97, 39)),
        BAN(new Color(183, 39, 11)),

        MESSAGE_DELETE(new Color(32, 73, 155)),

        NEUTRAL(new Color(24, 165, 45));

        private Color color;

        DiscordColor(Color color) {
            this.color = color;
        }

    }

}
