package campsite.structures;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyLinkedListTest {

    private MyLinkedList<String> list;

    @Before
    public void setUp() {
        list = new MyLinkedList<>();
    }

    @Test
    public void newListIsEmpty() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    public void addIncreasesSizeAndKeepsOrder() {
        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void getWithBadIndexThrows() {
        list.add("a");

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    public void removeHead() {
        list.add("a");
        list.add("b");

        assertTrue(list.remove("a"));
        assertEquals(1, list.size());
        assertEquals("b", list.get(0));
    }

    @Test
    public void removeMiddle() {
        list.add("a");
        list.add("b");
        list.add("c");

        assertTrue(list.remove("b"));
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void removeTailThenAddStillWorks() {
        list.add("a");
        list.add("b");
        list.remove("b");
        list.add("c");

        assertEquals(2, list.size());
        assertEquals("c", list.get(1));
    }

    @Test
    public void removeMissingItemReturnsFalse() {
        list.add("a");

        assertFalse(list.remove("zzz"));
        assertEquals(1, list.size());
    }

    @Test
    public void containsFindsExistingItemsOnly() {
        list.add("a");

        assertTrue(list.contains("a"));
        assertFalse(list.contains("b"));
    }

    @Test
    public void clearEmptiesTheList() {
        list.add("a");
        list.add("b");
        list.clear();

        assertTrue(list.isEmpty());
        assertNull(list.getHead());
    }
}