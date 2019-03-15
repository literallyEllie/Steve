package de.elliepotato.steve.chatmod;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.help.DumbResponder;
import de.elliepotato.steve.config.FileHandler;
import de.elliepotato.steve.module.DataHolder;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.UtilTime;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ellie for VentureNode LLC
 * at 04/02/2018
 */
public class MessageChecker extends ListenerAdapter implements DataHolder {

    private final int MAX_MESSAGE_TAG = 8;
    private final int MAX_AD_LINE = 8; // line breaks
    private final Pattern REGEX_DOMAIN = Pattern.compile("\\b((https?:/{2}(w{3}\\.)?)|(w{3}\\.))([^:/\\?\\=]+)\\b", Pattern.MULTILINE);
    private Steve bot;

    private FileHandler<Set<String>> domainsFile, blacklistedFile;
    private Set<String> allowedDomains, blacklistedDomains;

    private Map<Long, Long> advertCooldown;
    private long requiredAdCooldown;

    private MessageHistory messageHistory;
    private DumbResponder dumbResponder;

    /**
     * The big bad chat moderating module.
     *
     * @param bot The bot instance.
     */
    public MessageChecker(Steve bot) {
        this.bot = bot;
        this.messageHistory = new MessageHistory(bot);
        this.domainsFile = new DomainsFile(bot);
        this.allowedDomains = Sets.newHashSet();
        this.blacklistedFile = new BlacklistedDomainsFile(bot);
        this.blacklistedDomains = Sets.newHashSet();
        try {
            this.allowedDomains = domainsFile.read();
            this.blacklistedDomains = blacklistedFile.read();
        } catch (IOException e) {
            bot.getLogger().error("Failed to load domains!", e);
            e.printStackTrace();
        }
        this.dumbResponder = new DumbResponder(bot);

        this.advertCooldown = Maps.newHashMap();
        this.requiredAdCooldown = TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public void shutdown() {
        if (messageHistory != null)
            messageHistory.shutdown();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().getIdLong() == Constants.PRESUMED_SELF.getIdLong()) return;

        long channelId = event.getChannel().getIdLong();
        if (channelId == Constants.CHAT_BISECT_MOD.getIdLong() || channelId == Constants.CHAT_MELON_MOD.getIdLong())
            return;

        if (!bot.getDebugger().getEnabled() &&
                !PermissionUtil.canInteract(event.getGuild().getMember(bot.getJda().getUserById(Constants.PRESUMED_SELF.getIdLong())), event.getMember()))
            return;

        if (!tagCheck(event.getMessage())) return;
        if (!advertCheck(event.getMessage())) return;
        if (!messageHistory.call(event.getMessage())) return;

        dumbResponder.onMessage(event.getMessage());

        // ...
    }

    /**
     * Check the message for any tag spamming
     *
     * @param message The message to check
     * @return "true" if they are safe, "false" if they got rekt.
     */
    private boolean tagCheck(Message message) {
        String content = message.getContentRaw();

        final Matcher matcher = bot.getPatternUser().matcher(content);
        int hits = 0;
        while (matcher.find()) {

            final long id = Long.parseLong(matcher.group(1));
            if (id == Constants.STAFF_MAX.getIdLong()
                    || id == Constants.STAFF_ANDREW.getIdLong()
                    || id == Constants.STAFF_DANIEL.getIdLong()
                    || id == Constants.STAFF_AMBER.getIdLong()
                    || id == Constants.STAFF_JACOB.getIdLong()) {

                boolean bh = message.getGuild().getIdLong() == Constants.GUILD_BISECT.getIdLong();

                bot.tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + ", Please **do not tag** "
                        + (bh ? "BH" : "MC") + " staff, they will not reply. Instead please wait for a ask in one of the **help channels**" +
                        " or **open a ticket**. (If your request was long, ask the Discord Moderators for it back)", 10, null);

                message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Tagging official staff", message.getTextChannel())));
                return false;
            }

            hits++;

            final int finalHits = hits;
            if (finalHits > MAX_MESSAGE_TAG) {
                message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Tag spamming", message.getTextChannel())));
                return false;
            }

        }

        return true;
    }

    /**
     * Check if the message contains a bad advertisement and also check the message length of a potential advertisement.
     *
     * @param message Message to check.
     * @return "true" if they are safe, "false" if they got rekt.
     */
    public boolean advertCheck(Message message) {
        String content = message.getContentRaw();
        // if channel isn't advertise channel
        final boolean strict = message.getChannel().getIdLong() != Constants.CHAT_BISECT_AD.getIdLong() &&
                message.getChannel().getIdLong() != Constants.CHAT_MELON_AD.getIdLong();

        Matcher matcher = Pattern.compile("\n").matcher(content);

        // stop unnecessary check, "if channel is an advert channel"
        if (!strict) {
            int lineBreaks = 0;
            while (matcher.find()) {
                lineBreaks++;
                if (lineBreaks > MAX_AD_LINE) {

                    bot.tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + "Your advert is **too long**, please reconsider the size to make it smaller." +
                            " (Must fit in " + MAX_AD_LINE + " lines). If you want your advert back, please check your PMs.", 15, null);
                    bot.privateMessage(message.getAuthor(), "Your deleted advert: \n```" + message.getContentRaw() + "```");

                    message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Advert too big (too many lines - > " + MAX_AD_LINE + ")",
                            message.getTextChannel())));
                    return false;
                }

            }

            long lastMsg = advertCooldown.getOrDefault(message.getAuthor().getIdLong(), -1L);
            if (lastMsg != -1) {
                bot.getDebugger().write("last message existed");

                if (!UtilTime.elapsed(lastMsg, requiredAdCooldown)) {
                    bot.getDebugger().write("not elapsed");

                    bot.tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + "Your last advert was posted less than 24 hours ago, " +
                                    "please wait and then post. If this is a revised advert please message a Discord Moderator and they can help you.", 10,
                            message);

                    message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Advert posted before 24 hours waiting", message.getTextChannel())));
                    return false;
                }
            }

            bot.getDebugger().write("all good");
        }

        bot.getDebugger().write("Strict advert check, (message = " + content + ")");

        // Contains a URL?
        if (!Pattern.compile(".*\\b((https?:/{2}(w{3}\\.)?)|(w{3}\\.))(.*)?", Pattern.MULTILINE).matcher(content).matches() &&
                !Pattern.compile(".*\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b(:\\d{5})?.*", Pattern.MULTILINE).matcher(content).matches()) {
            // doesn't contain URL -- is in ad channel?
            if (!strict && bot.getDebugger().getEnabled()) {
                bot.getDebugger().write("strict: false\n" + "content: `" + content + "` \n" +
                        "match : " + content.matches(".*\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b(:\\d{5})?.*"));
                bot.tempMessage(message.getChannel().getIdLong(), message.getAuthor().getAsMention() + ", your message doesn't look like a server advert and will " +
                        "subsequently be deleted in **10 seconds**, please either talk in #general, if this is a mistake please tell a Discord Moderator.", 10, message);

                bot.modLog(message.getGuild(), embedMessageDelete(message, "It appears to be a non ad-message.", message.getTextChannel()));
                return false;
            }

            return true;
        }

        matcher = REGEX_DOMAIN.matcher(content);

        linkFinder:
        while (matcher.find()) {
            final String domain = matcher.group(5).toLowerCase().trim();

            if (strict) {
                // For every allowed domain
                for (String allowedDomain : allowedDomains) {
                    // Does the checking domain contain the allowed domain?
                    if (domain.contains(allowedDomain.replace("www.", "")))
                        // Ignore and carry on
                        continue linkFinder;

                }
            } else {
                bot.getDebugger().write("Unstrict check ");
                // If it is in an advert channel, check if the link is blacklisted or not.
                boolean hit = false;
                // For every denied domain
                for (String deniedLink : blacklistedDomains) {
                    if (domain.contains(deniedLink.replace("www.", ""))) {
                        hit = true;
                        break;
                    }
                }

                bot.getDebugger().write("deleting ? " + hit);
                if (!hit) {
                    if (bot.getDebugger().getEnabled()) {
                        advertCooldown.put(message.getAuthor().getIdLong(), System.currentTimeMillis());
                        bot.getDebugger().write("put in");
                    }
                    return true;
                }

            }

            // Tailor message
            bot.tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + ", your message contains a " + (!strict ? "blacklisted link." :
                    "non-authorised link, if you want to share links" +
                            " please say it in PMs.") + " If you want your message back, please ask staff.", 10, null);
            message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Advertising (" + (strict ? "unauthorised" : "blacklisted") +
                    " link) in #" + message.getChannel().getName(), message.getTextChannel()).addField("Match domain:", domain, true)));
            return false;
        }

        return true;
    }

    /**
     * Quick embed builder getter.
     *
     * @param message Message deleted.
     * @param reason  Reason message was deleted.
     * @param channel Channel message was deleted from.
     * @return The embed builder template, with the title and reason filled in.
     */
    private EmbedBuilder embedMessageDelete(Message message, String reason, TextChannel channel) {
        final User author = message.getAuthor();
        return bot.getEmbedBuilder(Steve.DiscordColor.MESSAGE_DELETE)
                .setTitle("Deleted message from " + author.getName() + "#" + author.getDiscriminator() + " (" + author.getIdLong() + ")" +
                        " in channel #" + channel.getName()).addField("Message content:", message.getContentRaw(), false)
                .addField("Reason:", reason, false);
    }

    /**
     * @return The allowed domains.
     */
    public Set<String> getAllowedDomains() {
        return allowedDomains;
    }

    /**
     * @return The domains file handler.
     */
    public FileHandler<Set<String>> getDomainsFile() {
        return domainsFile;
    }

    /**
     * @return the blacklisted domains handler.
     */
    public FileHandler<Set<String>> getBlacklistedFile() {
        return blacklistedFile;
    }

    /**
     * @return The preloaded blacklisted domains.
     */
    public Set<String> getBlacklistedDomains() {
        return blacklistedDomains;
    }

    /**
     * @return the thing that listens to messages and tries to help them.
     */
    public DumbResponder getDumbResponder() {
        return dumbResponder;
    }

    public Map<Long, Long> getAdvertCooldown() {
        return advertCooldown;
    }

}
