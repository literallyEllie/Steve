package de.elliepotato.steve.config;

import com.google.common.base.Preconditions;
import de.arraying.kotys.JSON;
import de.arraying.kotys.JSONField;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import java.io.*;

/**
 * @author Ellie for VentureNode LLC
 * at 03/02/2018
 */
public class JSONConfig {

    @JSONField(key = "botToken") private String botToken;
    @JSONField(key = "commandPrefix") private String commandPrefix;
    @JSONField(key = "gameType") private String gameType;
    @JSONField(key = "gameOf") private String gameOf;
    @JSONField(key = "botStatus") private String botStatus;

    /**
     * A JSON config handler
     */
    public JSONConfig() {
    }

    /**
     * Creates a template JSON config to fill out.
     * @param file The file to create.
     * @throws IOException Exception that will be throw if file null, error creating or writing to.
     *                      not handled here as can't log.
     */
    public void create(File file) throws IOException {
        Preconditions.checkNotNull(file, "Specified file to write to cannot be null");
        // No overwrite.
        if (file.exists()) return;
        if (!file.createNewFile()) throw new IOException("Failed to create file " + file.getName());

        final JSON template = new JSON()
                .put("botToken", "example")
                .put("commandPrefix", "!")
                .put("gameType", "DEFAULT")
                .put("gameOf", "Test test 123!")
                .put("botStatus", OnlineStatus.ONLINE.getKey())

                ;

        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(template.marshal());
        writer.close();

    }

    /**
     * Loads the configuration into memory.
     * @param file File to read from.
     * @return a new config instance
     * @throws IOException Exception that will be throw if file null or problem reading from file or error from "create".
     *                      not handled here as can't log.
     * @throws NullPointerException if a config value is null, hence not valid.
     *
     */
    public JSONConfig load(File file) throws IOException {
        Preconditions.checkNotNull(file, "Specified load file cannot be null");
        if (!file.exists()) {
            create(file);
            return null;
        }

        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();

        final String fileJsonStr = stringBuilder.toString();
        JSON json = new JSON(fileJsonStr);

        // validate date later.
        return json.marshal(JSONConfig.class);
    }

    /**
     * @return The bot token.
     */
    public String getBotToken() {
        return botToken;
    }

    /**
     * @return The the command prefix.
     */
    public String getCommandPrefix() {
        return commandPrefix;
    }

    /**
     * @return The game type, should be assignable to {@link net.dv8tion.jda.core.entities.Game.GameType}.
     */
    public String getGameType() {
        return gameType;
    }

    /**
     * @return Get the game that the bot will display as doing.
     */
    public String getGameOf() {
        return gameOf;
    }

    /**
     * @return Get the bot status, should be a key of {@link OnlineStatus}.
     */
    public String getBotStatus() {
        return botStatus;
    }

    /**
     * Check if the config instance to see if valid or not.
     * @throws NullPointerException If a value of the config is null. (Hence invalid)
     * @throws IllegalArgumentException If a value of the config is not acceptable. (Hence invalid)
     */
    public void validate() throws NullPointerException, IllegalArgumentException {
        // Null checks;
        Preconditions.checkNotNull(botToken, "Bot token cannot be null!");
        Preconditions.checkNotNull(botStatus, "Bot status cannot be null!");
        Preconditions.checkNotNull(commandPrefix, "Command prefix cannot be null!");
        Preconditions.checkNotNull(gameType, "Game type cannot be null!");
        Preconditions.checkNotNull(gameOf, "Game of cannot be null!");

        // Argument validation
        // will throw a IAE if bad
        Game.GameType.valueOf(gameType.toUpperCase());
        Preconditions.checkArgument(OnlineStatus.fromKey(botStatus.toLowerCase()) != OnlineStatus.UNKNOWN, "Online status not valid!");
    }

}
