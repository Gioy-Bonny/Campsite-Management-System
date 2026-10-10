package campsite.serivce;

import campsite.service.TextUtil;
import campsite.structures.MyLinkedList;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextUtilTest {

    @Test
    public void splitListTrimsItemsAndSkipsEmptyOnes() {
        MyLinkedList<String> items = TextUtil.splitList(" pool, WiFi ,, gym ,");

        assertEquals(3, items.size());
        assertEquals("pool", items.get(0));
        assertEquals("WiFi", items.get(1));
        assertEquals("gym", items.get(2));
    }

    @Test
    public void splitListOfNullOrBlankIsEmpty() {
        assertTrue(TextUtil.splitList(null).isEmpty());
        assertTrue(TextUtil.splitList("   ").isEmpty());
    }

    @Test
    public void joinWritesCommaSeparatedTextOrNone() {
        MyLinkedList<String> items = new MyLinkedList<>();
        assertEquals("none", TextUtil.join(items));

        items.add("pool");
        items.add("WiFi");
        assertEquals("pool, WiFi", TextUtil.join(items));
    }

    @Test
    public void euroAlwaysHasTwoDecimals() {
        assertEquals("EUR 240.00", TextUtil.euro(240));
        assertEquals("EUR 19.50", TextUtil.euro(19.5));
        assertEquals("EUR 0.05", TextUtil.euro(0.05));
    }
}
