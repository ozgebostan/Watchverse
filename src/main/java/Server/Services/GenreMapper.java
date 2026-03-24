package Server.Services;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility class to map TMDB genre IDs to their human-readable names.
 */
public class GenreMapper {

    // Immutable map to hold IDs and equivalent genre names
    private static final Map<Integer, String> GENRES;

    static {

        // --- Movie Genres ---

        // Make the map unmodifiable for thread safety and security
        GENRES = Map.ofEntries(Map.entry(28, "Action"), Map.entry(12, "Adventure"), Map.entry(16, "Animation"), Map.entry(35, "Comedy"), Map.entry(80, "Crime"), Map.entry(99, "Documentary"), Map.entry(18, "Drama"), Map.entry(10751, "Family"), Map.entry(14, "Fantasy"), Map.entry(36, "History"), Map.entry(27, "Horror"), Map.entry(10402, "Music"), Map.entry(9648, "Mystery"), Map.entry(10749, "Romance"), Map.entry(878, "Science Fiction"), Map.entry(10770, "TV Movie"), Map.entry(53, "Thriller"), Map.entry(10752, "War"), Map.entry(37, "Western"),

                // --- TV Series Specific Genres ---
                Map.entry(10759, "Action & Adventure"), Map.entry(10762, "Kids"), Map.entry(10763, "News"), Map.entry(10764, "Reality"), Map.entry(10765, "Sci-Fi & Fantasy"), Map.entry(10766, "Soap"), Map.entry(10767, "Talk"), Map.entry(10768, "War & Politics"));
    }

    /**
     * Returns the name of a single genre ID.
     */
    public static String getGenreName(int id) {
        return GENRES.getOrDefault(id, "Other");
    }

    public static String getGenreNames(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "Other";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (Integer id : ids) {
            String name = GENRES.get(id);
            if (name != null) {
                joiner.add(name);
            }
        }

        String result = joiner.toString();
        return result.isEmpty() ? "Other" : result;
    }
}