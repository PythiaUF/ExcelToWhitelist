package uf.pcbuilding.csvtowhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UsernameToUUID {
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private static final String UUID_URL = "https://playerdb.co/api/player/minecraft/%s";
    private static final Map<String, UUID> uuidCache = new HashMap<String, UUID>();

    private static final ExecutorService pool = Executors.newFixedThreadPool(4);

    private class Player {
        public UUID raw_id;
    }

    private class LookupResponseData {
        public Player player;
    }

    private class LookupResponse {
        public LookupResponseData data;
    }

    public static void getUUID(String username, Consumer<UUID> action) {
        pool.execute(() -> action.accept(getUUID(username)));
    }

    public static UUID getUUID(String username) {
        username = username.toLowerCase().strip();
        if (uuidCache.containsKey(username)) {
            return uuidCache.get(username);
        } else {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URI(String.format(UUID_URL, username)).toURL().openConnection();
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
            connection.setReadTimeout(3000);

            try {
                LookupResponse data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), LookupResponse.class);
                uuidCache.put(username, data.data.player.raw_id);
                return data.data.player.raw_id;
            } catch (IOException e) { // usually a 400
                return null;
            }


        }
    }
}
