package Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    private String now;

    @BeforeEach
    @DisplayName("Time of the current system")
    void setTime() {
        now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @DisplayName("With valid data, Item object should be created successfully")
    void testValidItemCreation() {


        Item item = new Item("Inception", "MOVIE", "Sci-Fi", "123", "url", 3, 148, 2016, now);

        assertEquals("Inception", item.title());
        assertEquals(3, item.priority());
        assertEquals(148, item.duration());
        assertEquals(now, item.addedDate());
    }

    @Test
    @DisplayName("When an empty title send, it should be initialized as 'Unknown Title'")
    void testInvalidTitle() {
        Item item = new Item("", "MOVIE", "Action", "456", "url", 2, 120, 2003, now);
        assertEquals("Unknown Title", item.title());
    }

    @Test
    @DisplayName("Invalid priority, should be 1-3")
    void testPriorityBoundaries() {
        Item itemHigh = new Item("Test", "MOVIE", "G", "ID", "URL", 5, 100, 2024, now);
        assertEquals(1, itemHigh.priority());

        Item itemLow = new Item("Test", "MOVIE", "G", "ID", "URL", -1, 100, 2024, now);
        assertEquals(1, itemLow.priority());
    }

    @Test
    @DisplayName("Negative duration should be initialized as 0")
    void testNegativeDuration() {
        Item item = new Item("Test", "MOVIE", "G", "ID", "URL", 2, -50, 2000, now);
        assertEquals(0, item.duration());
    }

    @Test
    @DisplayName("Equals method should only look at apiId")
    void testEqualsAndHashCode() {
        Item item1 = new Item("Film A", "MOVIE", "Action", "SAME_ID", "url1", 1, 100, 1999, now);
        Item item2 = new Item("Film B", "MOVIE", "Drama", "SAME_ID", "url2", 3, 200, 1999, now);

        assertEquals(item1, item2, "Item with same API ID's are same!");
        assertEquals(item1.hashCode(), item2.hashCode(), "Hash codes must be same!");
    }
}