package de.elliepotato.steve.status.minecraft;

import com.google.common.collect.Maps;
import de.arraying.kotys.JSON;
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

    private final Steve steve;

    private long lastFetch;
    private final Map<MCService, MCServiceStatus> lastResponse;

    public FetcherMCStatus(Steve steve) {
        this.steve = steve;
        this.lastResponse = Maps.newHashMap();

        for (MCService service : MCService.values()) {
            lastResponse.put(service, MCServiceStatus.UNKNOWN);
        }
    }

    @Override
    public void fetch() {
        try {
            // get data
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

            // returns an array of services and their respective status like [{"minecraft.net":"green"}, ..]
            final JSONArray json = new JSONArray(fullResponse.toString());
            // access {"minecraft.net":"green"}

            for (int i = 0; i < json.length(); i++) {
                final Map<String, Object> serviceStatus = json.json(i).raw();

                for (MCService value : MCService.values()) {
                    final Object status = serviceStatus.get(value.getRaw());

                    if (status == null)
                        continue;

                    if ((!(status instanceof String))) {
                        steve.getLogger().error("unrecognized status of " + serviceStatus);
                        break;
                    }

                    lastResponse.put(value, MCServiceStatus.fromString((String) status));
                }
            }

            lastFetch = System.currentTimeMillis();

        } catch (IOException e) {
            steve.getLogger().error("error whilst parsing Mojang response", e);
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
