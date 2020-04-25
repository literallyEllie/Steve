package de.elliepotato.steve.chatmod.checks.help;

import com.google.common.base.Joiner;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.cmd.commands.CmdPromo;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Ellie for VentureNode LLC
 * at 05/03/2018
 */
public class DumbResponder implements MessageCheck {

    private Steve steve;

    /**
     * A thing that just takes a message and see if it can help.
     * It is dumb because its just a bunch of ifs.
     *
     * @param steve Bot instance.
     */
    public DumbResponder(Steve steve) {
        this.steve = steve;
    }

    /**
     * The method to call on a message is sent.
     *
     * @param message the message sent.
     */
    public boolean check(Message message) {
        final Guild guild = message.getGuild();
        final Member member = message.getMember();
        final String msg = message.getContentRaw().toLowerCase();

        if (msg.contains("daemon") && (msg.contains("110") || msg.contains("111"))) {
            steve.messageChannel(message.getChannel().getIdLong(), "Hi there, " + member.getAsMention() + ". " +
                    "If you were talking about your **console log** not showing, it is **just temporary** and should be back soon! " +
                    "If it persists after about 15 minutes you can open a ticket");
            return true;
        }

        if (msg.matches("(are )?there any( current)? promo(tional)? codes( currently)?\\??")
                || msg.matches("(is|are)( there )?a(ny)? (sales|promo(tional)? codes?)?( currently)?\\??")) {

            List<String> codes = ((CmdPromo) steve.getCommandManager().getCommand("promo", false)).getCodesOf(guild);
            steve.messageChannel(message.getChannel().getIdLong(), codes.isEmpty() ? "Sorry there are currently no promotional codes!" :
                    "Current codes: " + Joiner.on(", ").join(codes));
            return true;
        }

        if (msg.matches("((can someone|i need) )?help( me)?( please)?\\??")) {

            String newMsg = "Hi there, " + member.getAsMention() + ". If you are looking for help please **specify the problems** you're experiencing " +
                    "(if you haven't already) as well as **posting any problems in console** via http://hastebin.com/ (if necessary). A real person will get to you soon!";

            if (message.getChannel().getIdLong() == Constants.CHAT_BISECT_GENERAL.getIdLong() || message.getChannel().getIdLong() ==
                    Constants.CHAT_MELON_GENERAL.getIdLong()) {
                newMsg += " **Also** could you ask your question in " + (guild.getIdLong() == Constants.GUILD_BISECT.getIdLong() ?
                        Constants.CHAT_BISECT_HELP_SPIGOT_CRAFT_VAN.toString() + " or " + Constants.CHAT_BISECT_HELP_MODDED.toString() + ", if it doesn't fit into those categories ask in "
                                + Constants.CHAT_BISECT_HELP_OTHER.toString()
                        : Constants.CHAT_MELON_HELP_SPIGOT_CRAFT_VAN.toString() + " or " + Constants.CHAT_MELON_HELP_MODDED + ", if it doesn't fit into those categories ask in "
                        + Constants.CHAT_MELON_HELP_OTHER) + " Thank you! :smile:";
            }

            steve.messageChannel(message.getChannel().getIdLong(), newMsg);
        }

        return true;
    }

}
