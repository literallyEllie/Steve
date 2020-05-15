package de.elliepotato.steve.chatmod.file;

import com.google.common.collect.Sets;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.config.FileHandler;

import java.io.*;
import java.util.Set;

/**
 * @author Ellie for VentureNode LLC
 * at 30/04/2020
 */
public class IgnoreFile extends FileHandler<Set<Long>> {

    /**
     * A handle for users that the bot should ignore.
     *
     * @param steve the bot instance.
     */
    public IgnoreFile(Steve steve) {
        super(steve, new File("ignored.txt"));
    }

    @Override
    public void writeDefaults() {
    }

    @Override
    public Set<Long> read() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(getFile()));
        final Set<Long> ignored = Sets.newHashSet();

        String line;
        while ((line = reader.readLine()) != null) {
            ignored.add(Long.valueOf(line.trim().replace("\n", "")));
        }

        reader.close();
        return ignored;
    }

    @Override
    public void write(Set<Long> type) throws IOException {
        final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(getFile()));
        for (Long ignored : type) {
            bufferedWriter.write(ignored + "\n");
        }
        bufferedWriter.close();
    }

}
