package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public record Item(
        String title,
        String type,
        String genres,
        String apiId,
        String posterUrl,
        int priority, // 1: Low, 2: Medium, 3: High
        int duration  // Minutes
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public Item {
        title = (title == null || title.isBlank()) ? "Unknown Title" : title;
        priority = (priority < 1 || priority > 3) ? 1 : priority;
        duration = Math.max(0, duration);
    }

    public Item withRealDuration(int realDuration) {
        return new Item(title, type, genres, apiId, posterUrl, priority, realDuration);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(apiId, item.apiId);
    }

    @Override
    public int hashCode() { return Objects.hash(apiId); }

    @Override
    public String toString() {
        return String.format("%s (P:%d, %d min)", title, priority, duration);
    }
}