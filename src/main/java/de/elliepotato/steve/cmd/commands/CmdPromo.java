package de.elliepotato.steve.cmd.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @author Ellie for VentureNode LLC
 * at 22/05/2018
 */
public class CmdPromo extends Command {

    private Map<Long, List<String>> codes;

    /**
     * A command for auto responses of promotional deals.
     *
     * @param steve the bot instance.
     */
    public CmdPromo(Steve steve) {
        super(steve, "promo", "Tell Steve about the new hot promotions", Lists.newArrayList(), Permission.KICK_MEMBERS, "[code]");
        this.codes = Maps.newHashMap();
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final long idLong = environment.getChannel().getGuild().getIdLong();

        final String[] args = environment.getArgs();

        if (args.length < 1) {
            getBot().messageChannel(environment.getChannel(), correctUsage());
            if (codes.containsKey(idLong)) {
                getBot().messageChannel(environment.getChannel(), "Current promo codes:\n" + Joiner.on("\n").join(codes.get(idLong)));
            }
            return;
        }

        String code = args[0];

        boolean added = false;

        if (!codes.containsKey(idLong)) {
            codes.put(idLong, Lists.newArrayList(code));
            added = true;
        } else if (codes.get(idLong).contains(code)) {
            codes.get(idLong).remove(code);
        } else {
            codes.get(idLong).add(code);
            added = true;
        }

        getBot().tempMessage(environment.getChannel(), (added ? ":thumbsup: Added" : ":thumbsdown: Removed") + " the promo code " + code + ".", 7,
                null);
    }

    public List<String> getCodesOf(Guild guild) {
        return codes.getOrDefault(guild.getIdLong(), Lists.newArrayList());
    }

}
