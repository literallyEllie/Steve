package de.elliepotato.steve.sheets.request;

public enum SheetsRequestMode {

    READ("GET"),
    WRITE("PUT"),
    BATCH_WRITE("POST");

    private final String method;

    SheetsRequestMode(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

}
