package de.elliepotato.steve.sheets;

import com.google.common.collect.Lists;
import com.mysql.cj.xdevapi.JsonArray;
import de.arraying.kotys.JSON;
import de.arraying.kotys.JSONArray;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.sheets.request.SheetsRequestBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class SheetsApi {

    private final String authKey;
    private final boolean enabled;

    private OkHttpClient client;

    /**
     * Sheets API wrapper
     *
     * Only supports reading for now.
     */
    public SheetsApi(Steve steve) {
        authKey = steve.getConfig().getSheetsAuthKey();
        enabled = authKey != null;

        if (enabled)
            client = new OkHttpClient();
    }

    public String[][] readSheet(SheetsRequestBuilder requestBuilder) {
        if (!enabled)
            return null;

        String url = requestBuilder.buildUrl(authKey);

        Request request = new Request.Builder()
                .url(url)
                .get() // TODO adjust to the type
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Steve-0 " + Steve.VERSION + " Discord Bot")
                .build();

        try (Response response = client.newCall(request).execute()) {
            final String rawResponse = response.body().string();
            JSON parsedResponse = new JSON(rawResponse);

            final JSONArray jsonValues = parsedResponse.array("values");

            String[][] values = new String[jsonValues.length()][];

            int topIndex = 0;
            for (Object topValue : jsonValues.toArray()) {

                if (topValue instanceof JSONArray) {
                    JSONArray contents = (JSONArray) topValue;

                    String[] subArray = new String[contents.length()];

                    int subIndex = 0;
                    for (Object subValue : contents.toArray()) {
                        subArray[subIndex++] = subValue.toString();
                    }

                    values[topIndex++] = subArray;
                } else {
                    values[topIndex++] = new String[]{"invalid json array at " + topValue};
                    // problem
                }

            }

            return values;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void writeSheet(String url) {
        throw new UnsupportedOperationException("writing to a sheet is not supported yet");
    }

}
