package de.elliepotato.steve.booster;

import com.google.common.collect.Maps;
import de.elliepotato.steve.util.UtilTime;
import net.dv8tion.jda.api.entities.Member;

import java.util.Map;

/**
 * @author Ellie :: 03/09/2019
 */
public class BoosterWatcherTask implements Runnable {

    private BoosterWatcher boosterWatcher;
    private WatcherCallback watcherCallback;

    // Guild ID, booster
    private Map<Long, Map<Long, GuildBooster>> liveBoosters, recentlyStoppedBoosters;

    public BoosterWatcherTask(BoosterWatcher boosterWatcher) {
        this.boosterWatcher = boosterWatcher;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("steve-booster-watcher");

        boosterWatcher.getSteve().getLogger().info("Running booster checker task...");

        liveBoosters = Maps.newHashMap();
        recentlyStoppedBoosters = Maps.newHashMap();

        int newBoosters = 0;
        int stoppedBoosters = 0;

        for (Map.Entry<Long, Map<Long, GuildBooster>> boosterEntry : boosterWatcher.getStoredBoosters().entrySet()) {
            final Long guildId = boosterEntry.getKey();

            final Map<Long, GuildBooster> storedBoosters = boosterEntry.getValue();

            final Map<Long, Member> liveBoosters = Maps.newHashMap();
            for (Member member : boosterWatcher.getLiveBoostersOf(guildId)) {
                liveBoosters.put(member.getIdLong(), member);
            }

            Map<Long, GuildBooster> updatedLiveBoosters = Maps.newHashMap();

            for (Map.Entry<Long, Member> liveBooster : liveBoosters.entrySet()) {
                final Long boosterId = liveBooster.getKey();
                final Member boosterMember = liveBooster.getValue();

                if (!storedBoosters.containsKey(boosterId)) {
                    // they are new.

                    GuildBooster guildBooster = new GuildBooster(boosterId,
                            boosterMember.getUser().getName() + "#" + boosterMember.getUser().getDiscriminator(), guildId, UtilTime.now());
                    guildBooster.setJustDiscovered(true);
                    newBoosters++;

                    updatedLiveBoosters.put(boosterId, guildBooster);
                    continue;
                }

                // Anyone else is still boosting.
                updatedLiveBoosters.put(boosterId, storedBoosters.get(boosterId));
            }

            // Compare updated and stored.

            for (GuildBooster oldBooster : storedBoosters.values()) {

                // they have stopped boosting.
                if (!updatedLiveBoosters.containsKey(oldBooster.getBoosterId())) {
                    if (!recentlyStoppedBoosters.containsKey(guildId))
                        recentlyStoppedBoosters.put(guildId, Maps.newHashMap());

                    recentlyStoppedBoosters.get(guildId).put(oldBooster.getBoosterId(), oldBooster);
                    stoppedBoosters++;
                }

            }

            // Store live boosters.
            if (!this.liveBoosters.containsKey(guildId))
                this.liveBoosters.put(guildId, Maps.newHashMap());

            this.liveBoosters.get(guildId).putAll(updatedLiveBoosters);
        }

        boosterWatcher.getSteve().getLogger().info("Booster task finished. New boosters: " + newBoosters + ". Stopped boosters: " + stoppedBoosters);

        if (watcherCallback != null)
            watcherCallback.onWatcherCheck();


    }

    public Map<Long, Map<Long, GuildBooster>> getLiveBoosters() {
        return liveBoosters;
    }

    public Map<Long, Map<Long, GuildBooster>> getRecentlyStoppedBoosters() {
        return recentlyStoppedBoosters;
    }

    public void setWatcherCallback(WatcherCallback watcherCallback) {
        this.watcherCallback = watcherCallback;
    }

}
