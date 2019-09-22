package de.elliepotato.steve.util;

/**
 * @author Ellie :: 18/07/2018
 */
public class UtilTime {

    public static boolean elapsed(long from, long required) {
        return System.currentTimeMillis() - from > required;
    }

    public static long now() {
        return System.currentTimeMillis();
    }

}
