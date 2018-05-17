package de.elliepotato.steve.chatmod;

import com.google.common.collect.Sets;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.config.FileHandler;

import java.io.*;
import java.util.Set;

/**
 * @author Ellie for VentureNode LLC
 * at 11/02/2018
 */
public class DomainsFile extends FileHandler<Set<String>> {

    /**
     * A handle for domains that are whitelisted throughout the help and general channels.
     *
     * @param steve the bot instance.
     */
    public DomainsFile(Steve steve) {
        super(steve, new File("domains.txt"));
    }

    @Override
    public void writeDefaults() throws IOException {
        final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(getFile()));
        for (String defaultAllowedDomain : Sets.newHashSet("hastebin.com", "pastebin.com", "google.com", "google.co.uk", "google.no",
                "meloncube.net", "bisecthosting.com", "discord.gg", "discordapp.com", "dis.gd", "discord.co", "discord.com", "spigotmc.org",
                "bukkit.org", "minecraft.net", "mojang.com", "minecraftforge.net", "wikipedia.org", "stackoverflow.com", "prnt.sc", "imgur.com",
                "strawpoll.me", "strawpoll.com", "github.com", "mc-market.org", "ess3.net", "ess3.net", "filezilla-project.org", "youtube.com",
                "mc-ess.net", "spotify.com")) {
            bufferedWriter.write(defaultAllowedDomain + "\n");
        }
        bufferedWriter.close();
    }

    @Override
    public Set<String> read() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(getFile()));
        final Set<String> allowedDomains = Sets.newHashSet();

        String line;
        while ((line = reader.readLine()) != null) {
            allowedDomains.add(line.trim().replace("\n", ""));
        }

        reader.close();
        return allowedDomains;
    }

    @Override
    public void write(Set<String> type) throws IOException {
        final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(getFile()));
        for (String allowedDomain : type) {
            bufferedWriter.write(allowedDomain + "\n");
        }
        bufferedWriter.close();
    }

}
