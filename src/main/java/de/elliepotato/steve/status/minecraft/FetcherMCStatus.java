package de.elliepotato.steve.status.minecraft;

import com.google.common.collect.Maps;
import de.arraying.kotys.JSONArray;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.status.StatusFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author Ellie for VentureNode LLC
 * at 22/05/2018
 */
public class FetcherMCStatus implements StatusFetcher<Map<MCService, MCServiceStatus>> {

    private Steve steve;

    private long lastFetch;
    private Map<MCService, MCServiceStatus> lastResponse;

    public FetcherMCStatus(Steve steve) {
        this.steve = steve;
    }

    @Override
    public void fetch() {

        try {

            final URL url = new URL("https://status.mojang.com/check");
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            final StringBuilder fullResponse = new StringBuilder();

            while ((line = in.readLine()) != null) {
                fullResponse.append(line);
            }
            in.close();
            con.disconnect();

            final JSONArray json = new JSONArray(fullResponse.toString());

            if (lastResponse == null)
                lastResponse = Maps.newHashMap();
            else lastResponse.clear();

            for (int i = 0; i < json.length(); i++) {
                MCService service = MCService.values()[i];
                final MCServiceStatus serviceStatus = MCServiceStatus.fromString(json.json(i).string(service.getRaw()));

                lastResponse.put(service, serviceStatus);
            }

            lastFetch = System.currentTimeMillis();

        } catch (IOException e) {
            steve.getLogger().error("Error whilst parsing Mojang response");
            e.printStackTrace();
        }

    }

    @Override
    public Map<MCService, MCServiceStatus> getLastFetch() {
        return lastResponse;
    }

    @Override
    public long lastFetch() {
        return lastFetch;
    }

}
