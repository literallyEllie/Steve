package de.elliepotato.steve.status.minecraft;

/**
 * @author Ellie for VentureNode LLC
 * at 22/05/2018
 */
public enum MCServiceStatus {

    GREEN("Good!"),
    YELLOW("Not so good."),
    RED("Bad :("),
    UNKNOWN("Unknown");

    private final String pretty;

    MCServiceStatus(String pretty) {
        this.pretty = pretty;
    }

    public static MCServiceStatus fromString(String input) {
        for (MCServiceStatus serviceStatus : values()) {
            if (serviceStatus.name().equalsIgnoreCase(input)) {
                return serviceStatus;
            }
        }

        return UNKNOWN;
    }

    public String getPretty() {
        return pretty;
    }

}
