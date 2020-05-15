package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.status.minecraft.FetcherMCStatus;
import de.elliepotato.steve.status.minecraft.MCService;
import de.elliepotato.steve.status.minecraft.MCServiceStatus;
import de.elliepotato.steve.util.UtilEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Ellie for VentureNode LLC
 * at 22/05/2018
 */
public class CmdMCStatus extends Command {

    private final FetcherMCStatus mcStatus;

    /**
     * Check the current status of all official Mojang endpoints.
     *
     * @param steve Bot instance.
     */
    public CmdMCStatus(Steve steve) {
        super(steve, "mcstatus", "Shows the current status of the Minecraft Services", Lists.newArrayList(), Permission.MESSAGE_WRITE);

        mcStatus = new FetcherMCStatus(steve);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mcStatus.fetch();
            }
        }, TimeUnit.MINUTES.toMillis(2), TimeUnit.MINUTES.toMillis(5));
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        if (mcStatus.getLastFetch() == null) {
            environment.reply("The statuses has not been fetched yet. Check back shortly :smile:");
            return;
        }

        final EmbedBuilder embedBuilder = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL);
        embedBuilder.setTitle("Minecraft Server status")
                .setDescription("These stats are refreshed every 5 minutes");

        for (Map.Entry<MCService, MCServiceStatus> mcServiceMCServiceStatusEntry : mcStatus.getLastFetch().entrySet()) {
            final MCService service = mcServiceMCServiceStatusEntry.getKey();
            final MCServiceStatus status = mcServiceMCServiceStatusEntry.getValue();

            embedBuilder.addField(service.getPrettyName(), status.getPretty(), true);
        }

        embedBuilder.addField("Last Refresh", "vvvvv", true);
        embedBuilder.setTimestamp(Instant.ofEpochMilli(mcStatus.lastFetch()));

        environment.reply(embedBuilder.build());
    }

    public FetcherMCStatus getMcStatus() {
        return mcStatus;
    }

}
