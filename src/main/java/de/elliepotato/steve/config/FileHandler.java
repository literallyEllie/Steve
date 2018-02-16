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

    /**
     * A class representing the handler of a file.
     * Holding all the abstract methods that are good for the
     * handling of the files.
     *
     * The constructor also creates the file, and handles the error should it occur.
     *
     * @param steve The bot instance.
     * @param file The file the class instance will read/write from.
     */
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

    /**
     * Write the preset file defaults.
     * @throws IOException if there is an error with writing to file.
     */
    public abstract void writeDefaults() throws IOException;

    /**
     * Should read the file and return the file contents in the form of T
     * @return The file contents.
     * @throws IOException if there is an error reading from the file.
     */
    public abstract T read() throws IOException;

    /**
     * Should write to the file.
     * @param type The contents to writ.e
     * @throws IOException If there is an error writing to the file.
     */
    public abstract void write(T type) throws IOException;

    /**
     * @return the bot instance.
     */
    protected Steve getSteve() {
        return steve;
    }

    /**
     * @return the file the instance is handling.
     */
    public File getFile() {
        return file;
    }

}
