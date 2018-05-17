package de.elliepotato.steve.util;

/**
 * @author Ellie for VentureNode LLC
 * at 05/02/2018
 */
public class UtilString {

    /**
     * A util method to get the final arguements of a string array input.
     *
     * @param args  the arguments to split from.
     * @param start the start index in the array.
     * @return the extracted and built string.
     */
    public static String getFinalArg(final String[] args, final int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i != start) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        final String msg = sb.toString();
        sb.setLength(0);
        return msg;
    }

}
