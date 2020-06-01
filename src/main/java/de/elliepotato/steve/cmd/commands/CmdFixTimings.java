package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ellie for VentureNode LLC
 * at 16/03/2019
 */
public class CmdFixTimings extends Command {

    private final Pattern BROKEN_TIMINGS_REGEX = Pattern.compile("https://www.spigotmc.org/go/timingsrl=([a-z]{10})");
    private final String CORRECT_TIMINGS_URL = "https://timings.spigotmc.org/?url=";

    /**
     * A utility command to fix a broken timings link,
     *
     * @param steve the bot instance
     */
    public CmdFixTimings(Steve steve) {
        super(steve, "fixtimings", "Fix a broken timings link ", Lists.newArrayList("ft"), Permission.MESSAGE_WRITE,
                "<link>");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final String link = environment.getArgs()[0].trim();

        Matcher matcher = BROKEN_TIMINGS_REGEX.matcher(link);

        if (!matcher.matches()) {
            environment.replyBadSyntax("Not broken or invalid link.");
            return;
        }

        environment.replySuccess("Try this: " +  CORRECT_TIMINGS_URL + matcher.group(1));
    }

}
