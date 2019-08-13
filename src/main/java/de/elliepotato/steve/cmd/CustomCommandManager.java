package de.elliepotato.steve.cmd;

import com.google.common.collect.Maps;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.CustomCommand;
import de.elliepotato.steve.module.DataHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Ellie for VentureNode LLC
 * at 05/02/2018
 */
public class CustomCommandManager implements DataHolder {

    private final static String TABLE = "steve_custom_commands";
    private Steve steve;
    private Map<Long, Map<String, CustomCommand>> customCommands;

    /**
     * The internal manager to handle guild custom commands created within the bot.
     *
     * @param steve the bot instance.
     */
    public CustomCommandManager(Steve steve) {
        this.steve = steve;
        this.customCommands = Maps.newHashMap();

        try (Connection connection = steve.getSqlManager().getConnection()) {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE + "` (" +
                    "`id` INT(100) NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                    "`guild` BIGINT NOT NULL, " +
                    "`label` TEXT NOT NULL, " +
                    "`description` TEXT NULL, " +
                    "`response` TEXT NULL, " +
                    "INDEX(guild)) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1").execute();

            final ResultSet resultSet = connection.prepareStatement("SELECT * FROM `" + TABLE + "`").executeQuery();
            while (resultSet.next()) {
                final long guild = resultSet.getLong("guild");
                final String label = resultSet.getString("label");
                final String description = resultSet.getString("description");
                final String response = resultSet.getString("response");

                final CustomCommand customCommand = new CustomCommand(steve, label, (description == null ? "None provided" : description), guild, response);
                addCustomCommand(guild, customCommand, false);
            }
            resultSet.close();

        } catch (SQLException | NullPointerException e) {
            steve.getLogger().error("Failed to setup custom commands!", e);
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        customCommands.clear();
    }

    /**
     * @return All the custom commands. The Long value is the guild ID.
     */
    public Map<Long, Map<String, CustomCommand>> getCustomCommands() {
        return customCommands;
    }

    /**
     * @param guildId The guildId custom commands to get
     * @return All the custom commands of the guild, or an empty map if there are none.
     */
    public Map<String, CustomCommand> getCustomCommandsOf(long guildId) {
        Map<String, CustomCommand> commands = customCommands.getOrDefault(guildId, Maps.newHashMap());

        if (customCommands.containsKey(0L)) {
            commands.putAll(customCommands.get(0L));
        }

        return commands;
    }

    /**
     * Add a custom command.
     *
     * @param guildId       The owner of the custom command.
     * @param customCommand The custom command object to register.
     * @param newCommand    Is the command a new command or not? (i.e created/edited during session)
     */
    public void addCustomCommand(long guildId, CustomCommand customCommand, boolean newCommand) {

        if (!customCommands.containsKey(guildId)) {
            Map<String, CustomCommand> customCommandMap = Maps.newHashMap();
            customCommandMap.put(customCommand.getLabel().toLowerCase(), customCommand);
            customCommands.put(guildId, customCommandMap);
        } else {

            final Map<String, CustomCommand> customCommandMap = getCustomCommandsOf(guildId);
            if (customCommandMap.containsKey(customCommand.getLabel().toLowerCase())) {
                customCommandMap.replace(customCommand.getLabel().toLowerCase(), customCommand);
                return;
            }

            customCommandMap.put(customCommand.getLabel().toLowerCase(), customCommand);
        }

        if (newCommand) {

            new Thread(() -> {
                try (Connection connection = steve.getSqlManager().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `" + TABLE + "` (guild, label, description, response) VALUES "
                            + "(?, ?, ?, ?)");
                    preparedStatement.setLong(1, guildId);
                    preparedStatement.setString(2, customCommand.getLabel().toLowerCase());
                    preparedStatement.setString(3, customCommand.getDescription());
                    preparedStatement.setString(4, customCommand.getResponseMessage());
                    preparedStatement.execute();
                    preparedStatement.close();
                } catch (SQLException e) {
                    steve.getLogger().error("Failed to push custom command " + customCommand.getLabel() + "!");
                    e.printStackTrace();
                }
            }).run();

        }

    }

    /**
     * Delete a custom command; unloads from local cache and deletes remotely.
     *
     * @param guildId The owner of the custom command
     * @param label   The label of the custom command to delete.
     */
    public void deleteCustomCommand(long guildId, String label) {
        if (!customCommands.containsKey(guildId)) return;

        final CustomCommand customCommand = customCommands.get(guildId).get(label.toLowerCase());
        if (customCommand == null)
            return;

        try (Connection connection = steve.getSqlManager().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `" + TABLE + "` WHERE label = ?");
            preparedStatement.setString(1, customCommand.getLabel().toLowerCase());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            steve.getLogger().error("Failed to delete custom command!");
            e.printStackTrace();
        } finally {
            customCommands.get(guildId).remove(label.toLowerCase());
        }

    }

}
