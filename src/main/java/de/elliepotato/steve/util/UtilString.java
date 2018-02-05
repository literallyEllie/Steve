package de.elliepotato.steve.util;

/**
 * @author Ellie for VentureNode LLC
 * at 05/02/2018
 */
public class UtilString {

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
