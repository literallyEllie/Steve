package de.elliepotato.steve.chatmod.file;

import com.google.common.collect.Sets;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.config.FileHandler;

import java.io.*;
import java.util.Set;

/**
 * @author Ellie for VentureNode LLC
 * at 09/05/2018
 */
public class BlacklistedDomainsFile extends FileHandler<Set<String>> {

    public BlacklistedDomainsFile(Steve steve) {
        super(steve, new File("blacklisted.txt"));
    }

    @Override
    public void writeDefaults() {
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
