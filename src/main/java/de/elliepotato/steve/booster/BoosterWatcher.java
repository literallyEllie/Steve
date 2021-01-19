package de.elliepotato.steve.booster;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.module.DataHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Ellie :: 03/09/2019
 */
public class BoosterWatcher implements DataHolder, WatcherCallback {

    private final static String TABLE = "steve_boosters";
    //                                                  Max                     Ellie (Debug)
    private final Long[] REPORT_RECIPIENTS = new Long[]{303668659723829261L, 123188806349357062L};

    private final Steve steve;
    private final ScheduledExecutorService scheduledExecutorService;
    private Map<Long, Map<Long, GuildBooster>> boosters;
    private BoosterWatcherTask watcherTask;

    public BoosterWatcher(Steve steve) {
        this.steve = steve;
        this.boosters = Maps.newHashMap();

        try (Connection connection = steve.getSqlManager().getConnection()) {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE + "` (" +
                    "`id` INT(100) NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                    "`guild` BIGINT NOT NULL, " +
                    "`booster_id` BIGINT NOT NULL, " +
                    "`booster_name` TEXT NOT NULL, " +
                    "`started` BIGINT NOT NULL, " +
                    "INDEX(guild)) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1").execute();

            final ResultSet resultSet = connection.prepareStatement("SELECT * FROM `" + TABLE + "`").executeQuery();
            while (resultSet.next()) {

                final long boosterId = resultSet.getLong("booster_id");

                String boosterName;
                final User boosterUserObj = steve.getJda().getUserById(boosterId);
                if (boosterUserObj != null)
                    boosterName = boosterUserObj.getName() + "#" + boosterUserObj.getDiscriminator();
                else boosterName = resultSet.getString("booster_name");

                final long guild = resultSet.getLong("guild");
                final long startedBooster = resultSet.getLong("started");

                GuildBooster guildBooster = new GuildBooster(boosterId, boosterName, guild, startedBooster);

                if (!boosters.containsKey(guild))
                    boosters.put(guild, Maps.newHashMap());

                boosters.get(guild).put(boosterId, guildBooster);

            }

        } catch (SQLException e) {
            steve.getLogger().error("Failed to setup booster watcher sql!", e);
            e.printStackTrace();
        }

        // make watcher get.
        for (Guild guild : steve.getJda().getMutualGuilds(steve.getJda().getSelfUser())) {
            if (!boosters.containsKey(guild.getIdLong()))
                boosters.put(guild.getIdLong(), Maps.newHashMap());
        }

        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        watcherTask = new BoosterWatcherTask(this);
        watcherTask.setWatcherCallback(this);

        scheduledExecutorService.scheduleAtFixedRate(watcherTask, 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void shutdown() {

        this.boosters.clear();

        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }

        if (watcherTask != null) {
            if (watcherTask.getRecentlyStoppedBoosters() != null) watcherTask.getRecentlyStoppedBoosters().clear();
            if (watcherTask.getLiveBoosters() != null) watcherTask.getLiveBoosters().clear();
        }

    }

    public Steve getSteve() {
        return steve;
    }

    public Map<Long, Map<Long, GuildBooster>> getStoredBoosters() {
        return boosters;
    }

    public Map<Long, GuildBooster> getStoredBoostersOf(long guild) {
        return boosters.get(guild);
    }

    public List<Member> getLiveBoostersOf(long guild) {
        return steve.getJda().getGuildById(guild).getBoosters();
    }

    @Override
    public void onWatcherCheck() {
        this.boosters = watcherTask.getLiveBoosters();

        StringBuilder report = new StringBuilder();
        this.boosters.forEach((guildId, longGuildBoosterMap) -> {

            final List<String> collectedBoosters = longGuildBoosterMap.values().stream()
                    .filter(GuildBooster::isJustDiscovered)
                    .map(GuildBooster::getBoosterUsername)
                    .collect(Collectors.toList());

            if (!collectedBoosters.isEmpty())
                report.append("New Boosters for server **").append(steve.getJda().getGuildById(guildId).getName()).append("**: ");

            report.append(Joiner.on(", ").join(collectedBoosters)).append("\n");
        });

        if (report.length() != 0) report.append("\n");

        Map<Long, Map<Long, GuildBooster>> recentlyStoppedBoosters = watcherTask.getRecentlyStoppedBoosters();

        recentlyStoppedBoosters.forEach((guildId, longGuildBoosterMap) -> {
            if (report.length() != 0) report.append("\n");

            if (!longGuildBoosterMap.isEmpty())
                report.append("Boosters who have stopped boosting server **").append(steve.getJda().getGuildById(guildId).getName()).append("**: ");

            report.append(Joiner.on(", ").join(longGuildBoosterMap.values().stream().map(GuildBooster::getBoosterUsername).collect(Collectors.toList()))).append("\n");
        });

        if (!report.toString().trim().isEmpty()) {
            for (Long reportRecipient : REPORT_RECIPIENTS) {
                final User recipientUser = steve.getJda().getUserById(reportRecipient);
                if (recipientUser != null)
                    recipientUser.openPrivateChannel().complete().sendMessage(report.toString()).queue();
                else
                    steve.getLogger().warn("could not report to UID " + reportRecipient);
            }

        }

        // delete SQL
        deleteStaleBoosters(recentlyStoppedBoosters.values());
        // update sql
        insertNewBoosters();

        this.boosters.values().forEach(longGuildBoosterMap -> longGuildBoosterMap.values().forEach(guildBooster -> guildBooster.setJustDiscovered(false)));
    }

    private void deleteStaleBoosters(Collection<Map<Long, GuildBooster>> boosters) {

        try (Connection connection = steve.getSqlManager().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + TABLE + "` WHERE guild = ? AND booster_id = ?");

            for (Map<Long, GuildBooster> serverBoosters : boosters) {
                for (GuildBooster guildBooster : serverBoosters.values()) {
                    statement.setLong(1, guildBooster.getBoosterGuild());
                    statement.setLong(2, guildBooster.getBoosterId());
                    statement.addBatch();
                }
            }

            statement.executeBatch();
        } catch (SQLException e) {
            steve.getLogger().error("Failed to delete stale boosters!");
            e.printStackTrace();
        }

    }

    private void insertNewBoosters() {
        try (Connection connection = steve.getSqlManager().getConnection()) {
            final PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + TABLE + "` (guild, booster_id, booster_name, started) VALUES (?, ?, ?, ?)");

            for (Map<Long, GuildBooster> value : this.boosters.values()) {
                for (GuildBooster guildBooster : value.values()) {
                    if (!guildBooster.isJustDiscovered()) continue;

                    statement.setLong(1, guildBooster.getBoosterGuild());
                    statement.setLong(2, guildBooster.getBoosterId());
                    statement.setString(3, guildBooster.getBoosterUsername());
                    statement.setLong(4, guildBooster.getStarted());
                    statement.addBatch();
                }
            }

            statement.executeBatch();

        } catch (SQLException e) {
            steve.getLogger().error("Failed to insert new boosters.");
            e.printStackTrace();
        }

    }

}
