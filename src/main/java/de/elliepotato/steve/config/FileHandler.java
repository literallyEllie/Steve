package de.elliepotato.steve.config;

import de.elliepotato.steve.Steve;

import java.io.File;
import java.io.IOException;

/**
 * @author Ellie for VentureNode LLC
 * at 1/02/2018
 */
public abstract class FileHandler<T> {

    private Steve steve;
    private File file;

    public FileHandler(Steve steve, File file) {
        this.steve = steve;
        this.file = file;

        if (!file.exists()) {
            try {
                file.createNewFile();
                writeDefaults();
            } catch (IOException e) {
                steve.getLogger().error("Failed to create file/set defaults " + file.getName() + "! " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public abstract void writeDefaults() throws IOException;

    public abstract T read() throws IOException;

    public abstract void write(T type) throws IOException;

    public Steve getSteve() {
        return steve;
    }

    public File getFile() {
        return file;
    }

}
