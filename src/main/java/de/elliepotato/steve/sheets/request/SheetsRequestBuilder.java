package de.elliepotato.steve.sheets.request;

import com.google.common.base.Preconditions;

import java.text.MessageFormat;

public class SheetsRequestBuilder {

    private static final String BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets/{0}",
            GATEWAY_VALUES = "/values/{0}!{1}:{2}";

    private final SheetsRequestMode requestMode;
    private String spreadSheetId, sheetName, lowerBound, upperBound;

    /**
     * Builder for building a request to the Google SheetsAPI
     *
     * @param requestMode request mode for this request
     */
    public SheetsRequestBuilder(SheetsRequestMode requestMode) {
        this.requestMode = requestMode;
    }

    public SheetsRequestMode getRequestMode() {
        return requestMode;
    }

    /**
     * Set the spreadsheet id.
     * <p>
     * In a normal URL it would be placed here https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/...
     *
     * @param spreadSheetId the spreadsheet id
     * @return the builder
     */
    public SheetsRequestBuilder setSpreadSheetId(String spreadSheetId) {
        this.spreadSheetId = spreadSheetId;
        return this;
    }

    /**
     * Sets the sheet name to use
     * <p>
     * It is the full name as shown on the web view.
     *
     * @param sheetName the sheet name to use
     * @return the builder
     */
    public SheetsRequestBuilder setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    /**
     * Set the lower selection bound for getting values.
     * <p>
     * This would be like selecting A1 to A3 thus A1 is the lower.
     *
     * @param lowerBound the lower bound to select
     * @return the builder
     */
    public SheetsRequestBuilder setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
        return this;
    }

    /**
     * Set the upper selection bound for getting values.
     * <p>
     * This would be like selecting A1 to A3 thus A3 is the upper.
     *
     * @param upperBound the upper bound to select
     * @return the builder
     */
    public SheetsRequestBuilder setUpperBound(String upperBound) {
        this.upperBound = upperBound;
        return this;
    }

    /**
     * Constructs a URl to make a request to for the provided data.
     * <p>
     * Preconditions are done to ensure everything is not-null or will throw an exception
     *
     * @param authKey the auth key to use
     * @return the constructed url to request to
     */
    public String buildUrl(String authKey) {
        Preconditions.checkNotNull(spreadSheetId, "spread sheet id cannot be null");
        Preconditions.checkNotNull(sheetName, "sheet id cannot be null");
        Preconditions.checkNotNull(lowerBound, "lower bound for values gateway cannot be null");
        Preconditions.checkNotNull(upperBound, "upper bound for values gateway cannot be null");
        Preconditions.checkNotNull(authKey, "auth key cannot be null");

        String url = MessageFormat.format(BASE_URL, spreadSheetId);
        String valueGateway = MessageFormat.format(GATEWAY_VALUES, sheetName, lowerBound, upperBound);

        return url + valueGateway + "?key=" + authKey;
    }

}
