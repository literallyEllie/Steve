package de.elliepotato.steve;

import com.google.common.base.Joiner;
import de.elliepotato.steve.booster.BoosterWatcher;
import de.elliepotato.steve.chatmod.MessageChecker;
import de.elliepotato.steve.cmd.CommandManager;
import de.elliepotato.steve.cmd.CustomCommandManager;
import de.elliepotato.steve.config.JSONConfig;
import de.elliepotato.steve.console.SteveConsole;
import de.elliepotato.steve.mysql.MySQLManager;
import de.elliepotato.steve.react.ReactManager;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.DebugWriter;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ellie for VentureNode LLC
 * at 03/02/2018
 */
public class Steve {

    public static final String VERSION = "1.5-DEV";
    public static final String[] AUTHORS = {"Ellie#0006"};

    private final Logger LOGGER = LoggerFactory.getLogger(Steve.class);
    private final DebugWriter DEBUG = new DebugWriter(this);

    private final Pattern PATTERN_USER = Pattern.compile("<@!?([0-9]+)>");

    private final File FILE_CONFIG = new File("config.json");
    private JSONConfig config;

    private JDA jda;
    private Thread mainThread;

    private CommandManager commandManager;
    private MySQLManager sqlManager;
    private CustomCommandManager customCommandManager;
    private ReactManager reactManager;

    private MessageChecker messageChecker;

    private BoosterWatcher boosterWatcher;

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
        this.mainThread = Thread.currentThread();
        init();
    }

    /**
     * Method to prepare the bot and start it.
     */
    private void init() {
        final long start = System.currentTimeMillis();

        LOGGER.info("");
        LOGGER.info("Starting Steve-0 bot v" + VERSION + " by " + Joiner.on(", ").join(AUTHORS));
        LOGGER.info("");

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
                return;
            }

        } catch (IOException e) {
            LOGGER.error("Failed to setup config! Please check the environment", e);
            return;
        }

        // Listener registration
        this.messageChecker = new MessageChecker(this);
        this.commandManager = new CommandManager(this);
        this.reactManager = new ReactManager(this);

        // Setup JDA (blocking).
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(config.getBotToken())
                    // Playing is now DEFAULT
                    .setActivity(Activity.of(Activity.ActivityType.valueOf(config.getGameType().toUpperCase()), config.getGameOf())) // we already know its valid.
                    .setStatus(OnlineStatus.fromKey(config.getBotStatus().toLowerCase()))
                    .addEventListeners(messageChecker, commandManager, reactManager)
                    .build()
                    .awaitReady();
        } catch (LoginException | InterruptedException e) {
            LOGGER.error("Failed to setup JDA", e);
            return;
        }

        this.sqlManager = new MySQLManager(this);
        this.customCommandManager = new CustomCommandManager(this);

        this.boosterWatcher = new BoosterWatcher(this);

        LOGGER.info("Steve startup completed in " + (System.currentTimeMillis() - start) + "ms. Console thread starting.");
        this.steveConsole = new SteveConsole(this);
        steveConsole.start();
    }

    /**
     * Shut down the bot safely then exits the program.
     *
     * @param exitCode What the program's exit code will be.
     *                 A Java "optional variable", if no input, it will be 0
     *                 Else, will be the first element to the array.
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

        if (boosterWatcher != null)
            boosterWatcher.shutdown();

        if (jda != null)
            jda.shutdownNow();

        if (steveConsole != null)
            steveConsole.interrupt();

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
     * @return Main thread of application.
     */
    public Thread getMainThread() {
        return mainThread;
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
     * @return The react manager.
     */
    public ReactManager getReactManager() {
        return reactManager;
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
     * @return The booster watcher.
     */
    public BoosterWatcher getBoosterWatcher() {
        return boosterWatcher;
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
     * Message any channel that implements {@link net.dv8tion.jda.api.entities.MessageChannel}
     *
     * @param channel Channel ID
     * @param message Message to send
     */
    public void messageChannel(MessageChannel channel, String message) {
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
     * Message any channel that implements {@link net.dv8tion.jda.api.entities.MessageChannel}
     *
     * @param channel Channel ID
     * @param message embed to send
     */
    public void messageChannel(MessageChannel channel, MessageEmbed message) {
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
     * Send a temporary message to a discord channel that implements {@link net.dv8tion.jda.api.entities.MessageChannel} (aka all of them)
     *
     * @param channel    The channel to send to
     * @param message    The message content
     * @param expireTime After what delay should the message be deleted
     */
    public void tempMessage(MessageChannel channel, String message, int expireTime, Message cleanupMsg) {
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
        final MessageEmbed build = embedBuilder.build();

        getLogger().info("[Mod-Log] " + guild.getName() + ": " + build.getTitle());
        messageChannel((guild.getIdLong() == Constants.GUILD_BISECT.getIdLong() ? Constants.CHAT_BISECT_MOD.getIdLong() : Constants.CHAT_MELON_MOD.getIdLong()), build);
    }

    /**
     * Attempt to parse from an input string to a {@link User}.
     * Will attempt to parse from: a raw ID, a mention or a User#Discrim
     *
     * @param input The input to parse from.
     * @return the user or null if failed to parse
     */
    public User parseUser(String input) {

        // raw id
        try {
            return jda.getUserById(Long.parseLong(input));
        } catch (NumberFormatException e) {
        }

        // a mention
        final Matcher matcher = PATTERN_USER.matcher(input);
        if (matcher.matches()) {
            try {
                return jda.getUserById(Long.parseLong(matcher.group(1)));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // accept user#discrim
        if (input.contains("#")) {

            final String[] parts = input.split("#");
            final String name = parts[0];
            final String discriminator = parts[1]; // not parsing to int cus 0006 == 6

            for (final User user : jda.getUsersByName(name, true)) {
                if (user.getName().equalsIgnoreCase(name) && user.getDiscriminator().equals(discriminator)) {
                    return user;
                }
            }

        }

        return null;
    }


}
