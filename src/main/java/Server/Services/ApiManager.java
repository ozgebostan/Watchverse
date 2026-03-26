package Server.Services;

import Model.Item;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * Manages TMDB API requests and parses JSON responses into Item objects.
 */
public class ApiManager {

    private static final String SEARCH_BASE_URL = "https://api.themoviedb.org/3/search/";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w185";
    private String apiKey;

    public ApiManager() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("TMDB_API_KEY");

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("Error: Couldn't found the TMDB_API_KEY .env file!");
        }
    }



    /**
     * Executes a search request to TMDB.
     */
    public String search(String query, String type) {

        System.out.println("[API] Kullanilan Key: " + apiKey);
        if (apiKey == null || apiKey.isEmpty()) return null;

        HttpURLConnection connection = null;
        try {
            String formattedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            // type: "movie" or "tv"
            String urlString = SEARCH_BASE_URL + type + "?api_key=" + apiKey + "&query=" + formattedQuery + "&language=tr-TR";

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int status = connection.getResponseCode();
            InputStream stream = (status >= 200 && status < 300) ? connection.getInputStream() : connection.getErrorStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }

        } catch (Exception e) {
            System.err.println("API Connection Error: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /**
     * Parses the JSON response into a list of Item records.
     */
    public List<Item> parseResponse(String jsonResponse, String type) {
        List<Item> items = new ArrayList<>();
        if (jsonResponse == null || jsonResponse.isEmpty()) return items;

        try {
            JSONObject responseObj = new JSONObject(jsonResponse);
            if (!responseObj.has("results")) return items;

            JSONArray results = responseObj.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject raw = results.getJSONObject(i);

                // Movies use "title", TV shows use "name"
                String title = raw.optString("title", raw.optString("name", "Unknown Content"));
                String apiId = String.valueOf(raw.optInt("id", 0));

                // Poster Path handling
                String posterPath = raw.optString("poster_path", null);
                String fullPosterUrl = (posterPath != null && !posterPath.isEmpty())
                        ? IMAGE_BASE_URL + posterPath
                        : null;

                // --- Genre Mapping (Using our improved GenreMapper) ---
                List<Integer> genreIdList = new ArrayList<>();
                JSONArray genreIdsJson = raw.optJSONArray("genre_ids");
                if (genreIdsJson != null) {
                    for (int j = 0; j < genreIdsJson.length(); j++) {
                        genreIdList.add(genreIdsJson.getInt(j));
                    }
                }
                // Tek satırda tüm türleri çözen yeni metodumuzu kullanıyoruz:
                String finalGenres = GenreMapper.getGenreNames(genreIdList);

                // Create the Item (Record style)
                items.add(new Item(
                        title,
                        type.toUpperCase(),
                        finalGenres,
                        apiId,
                        fullPosterUrl,
                        1,
                        0
                ));
            }
        } catch (Exception e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
        }
        return items;
    }
}