package Model;

import java.io.Serializable;

/**
 *Used for Discover feature,
 * a class that represent public watch lists
 */
public record PublicWatchlist(int id, String name) implements Serializable {

    private static final long serialVersionUID = 2L;

    public PublicWatchlist {
        if (name == null || name.isBlank()) {
            name = "Unnamed List";
        }
    }
}