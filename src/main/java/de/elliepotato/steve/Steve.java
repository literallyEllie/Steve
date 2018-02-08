package de.elliepotato.steve;

import de.elliepotato.steve.antispam.MessageChecker;
import de.elliepotato.steve.cmd.CommandManager;
import de.elliepotato.steve.cmd.CustomCommandManager;
import de.elliepotato.steve.config.JSONConfig;
import de.elliepotato.steve.console.SteveConsole;
import de.elliepotato.steve.mysql.MySQLManager;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
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

    public static final String VERSION = "1.0-RELEASE";

    private final Logger LOGGER = LoggerFactory.getLogger("Steve");

    private final Pattern PATTERN_USER = Pattern.compile("<@!?([0-9]+)>");

    private final File FILE_CONFIG = new File("config.json");
    private JSONConfig config;

    private JDA jda;

    private CommandManager commandManager;
    private MySQLManager sqlManager;
    private CustomCommandManager customCommandManager;

    private SteveConsole steveConsole;

    /**
     * A project for the hosting companies MelonCube and BisectHosting.
     * Since they both share the same parent company (VentureNode LLC), it was thought
     * a good name for the bot was to be Steve, as suggested by the CEO.
     * However this name wasn't available, so it was chosen for Steve-0 instead.
     *
     * MelonCube: https://www.meloncube.net/
     * BisectHosting: https://www.bisecthosting.com/
     *
     * The project is a joint Java-Kotlin project, most object models due to the nature of Kotlin
     * making it more efficient to do.
     *
     * This bot has no official affiliation with the the company and was simply created
     * in the light of volunteering to help improve the community and ease moderation.
     *
     * The bot will contain:
     *      - Custom commands;
     *      - Auto-moderation (i.e Spam detection);
     *      - Knowledge-Base quick searcher;
     *      - <i>More to be decided...</i>
     *
     *
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

        // Setup JDA (blocking).
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(config.getBotToken())
                    .setGame(Game.of(Game.GameType.valueOf(config.getGameType().toUpperCase()), config.getGameOf())) // we already know its valid.
                    .setStatus(OnlineStatus.fromKey(config.getBotStatus().toLowerCase()))
                    .addEventListener(new MessageChecker(this), commandManager)
                    .buildBlocking();
        } catch (LoginException | InterruptedException e) {
            LOGGER.error("Failed to setup JDA", e);
            e.printStackTrace();
            return;
        }

        this.sqlManager = new MySQLManager(this);
        this.customCommandManager = new CustomCommandManager(this);

        LOGGER.info("Steve startup completed in " + (System.currentTimeMillis() - start) +  "ms. Console thread starting.");
        this.steveConsole = new SteveConsole(this);
        steveConsole.run();

    }

    /**
     * Shut down the bot safely then exits the program.
     * @param exitCode What the program's exit code will be.
     *                 A Java "optional variable", if no input, it will be 0
     *                 Else, will be the first entry to the array.
     */
    public void shutdown(int... exitCode) {

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
     * Message a discord channel
     * @param channel Channel ID
     * @param message Message to send
     */
    public void messageChannel(long channel, String message) {
        jda.getTextChannelById(channel).sendMessage(message).queue();
    }

    /**
     * Message any channel that implements {@link Channel}
     * @param channel Channel ID
     * @param message Message to send
     */
    public void messageChannel(Channel channel, String message) {
        messageChannel(channel.getIdLong(), message);
    }

    /**
     * Message a discord channel
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
     * @param channel Channel ID
     * @param message embed to send
     */
    public void messageChannel(Channel channel, MessageEmbed message) {
        messageChannel(channel.getIdLong(), message);
    }

    /**
     * Send a temporary message to a discord channel
     * @param channel The channel to send to
     * @param message The message content
     * @param expireTime After what delay should the message be deleted
     */
    public void tempMessage(long channel, String message, int expireTime, Message cleanupMsg) {
        jda.getTextChannelById(channel).sendMessage(message).queue(message1 -> {
            message1.delete().queueAfter(expireTime, TimeUnit.SECONDS);
            if (cleanupMsg != null) cleanupMsg.delete().queueAfter(expireTime, TimeUnit.SECONDS);
        });
    }

    /**
     * Send a temporary message to a discord channel that implements {@link Channel} (aka all of them)
     * @param channel The channel to send to
     * @param message The message content
     * @param expireTime After what delay should the message be deleted
     */
    public void tempMessage(Channel channel, String message, int expireTime,  Message cleanupMsg) {
        tempMessage(channel.getIdLong(), message, expireTime, cleanupMsg);
    }

    /**
     * Send a private message to a user
     * @param user User to message
     * @param content the message content
     */
    public void privateMessage(User user, String content) {
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(content).queue());
    }

    /**
     * Send a private message to a member
     * @param member Member to message
     * @param content the message content
     */
    public void privateMessage(Member member, String content) {
        privateMessage(member.getUser(), content);
    }

    /**
     * Log a moderation action performed by the bot.
     * @param guild The guild to mod log to.
     * @param embedBuilder The embed to log
     */
    public void modLog(Guild guild, EmbedBuilder embedBuilder) {
        messageChannel((guild.getIdLong() == Constants.GUILD_BISECT.getIdLong() ? Constants.GUILD_BISECT.getIdLong() : Constants.GUILD_MELON.getIdLong()), embedBuilder.build());
    }

    /**
     * Get the default embed builder.
     * @param discordColor The color for the side bit to be
     * @return A embed builder set with a timestamp, color of choose
     *          and footer of "AssimilationMC Development Team" and a lovely cake picture.
     */
    public EmbedBuilder getEmbedBuilder(DiscordColor discordColor) {
        return new EmbedBuilder()
                .setColor(discordColor.color)
                .setTimestamp(Instant.now())
                .setFooter("Just Steve-0 doing his job.", null);
    }


    public enum DiscordColor {

        KICK(new Color(232, 97, 39)),
        BAN(new Color(183, 39, 11)),

        MESSAGE_DELETE(new Color(32, 73, 155)),

        NEUTRAL(new Color(24, 165, 45))

        ;

        private Color color;

        DiscordColor(Color color) {
            this.color = color;
        }

    }

}
