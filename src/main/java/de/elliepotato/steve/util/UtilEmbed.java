package de.elliepotato.steve.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;

/**
 * @author Ellie for VentureNode LLC
 * at 28/03/2019
 */
public class UtilEmbed {

    /**
     * Get the default embed builder.
     *
     * @param embedColor The color for the side bit to be
     * @return A embed builder set with a timestamp, color of choose
     * and footer of "AssimilationMC Development Team" and a lovely cake picture.
     */
    public static EmbedBuilder getEmbedBuilder(EmbedColor embedColor) {
        return new EmbedBuilder()
                .setColor(embedColor.color)
                .setTimestamp(Instant.now())
                .setFooter("Just Steve-0 doing his job.", null);
    }


    /**
     * Quick embed builder getter.
     *
     * @param message Message deleted.
     * @param reason  Reason message was deleted.
     * @param channel Channel message was deleted from.
     * @return The embed builder template, with the title and reason filled in.
     */
    public static EmbedBuilder moderatorDeletedMessage (Message message, String reason, TextChannel channel) {
        final User author = message.getAuthor();
        return getEmbedBuilder(EmbedColor.MESSAGE_DELETE)
                .setTitle("Deleted message from " + author.getName() + "#" + author.getDiscriminator() + " (" + author.getIdLong() + ")" +
                        " in channel #" + channel.getName()).addField("Message content:", message.getContentRaw(), false)
                .addField("Reason:", reason, false);
    }



    public enum EmbedColor {

        KICK(new Color(232, 97, 39)),
        BAN(new Color(183, 39, 11)),

        MESSAGE_DELETE(new Color(32, 73, 155)),

        NEUTRAL(new Color(24, 165, 45));

        private Color color;

        EmbedColor(Color color) {
            this.color = color;
        }

    }

}
