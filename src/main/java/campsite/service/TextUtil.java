package campsite.service;

import campsite.structures.MyLinkedList;
import campsite.structures.Node;

/**
 * Small text helpers shared by the services and the GUI.
 * They deliberately avoid arrays (String.split returns one), because the
 * exercise forbids using regular arrays directly.
 */
public final class TextUtil {

    private TextUtil() {
    }

    /**
     * Splits a comma separated text such as "pool, WiFi ,gym" into a list.
     * Items are trimmed and empty items are skipped.
     *
     * @param text the text to split (null gives an empty list)
     * @return the items in their original order
     */
    public static MyLinkedList<String> splitList(String text) {
        MyLinkedList<String> result = new MyLinkedList<>();
        if (text == null) {
            return result;
        }
        int start = 0;
        while (true) {
            int comma = text.indexOf(',', start);
            int end = (comma == -1) ? text.length() : comma;
            String item = text.substring(start, end).trim();
            if (!item.isEmpty()) {
                result.add(item);
            }
            if (comma == -1) {
                break;
            }
            start = comma + 1;
        }
        return result;
    }

    /**
     * Joins a list of strings into "a, b, c".
     *
     * @return the joined text, or "none" if the list is empty
     */
    public static String join(MyLinkedList<String> items) {
        if (items.isEmpty()) {
            return "none";
        }
        StringBuilder text = new StringBuilder();
        Node<String> current = items.getHead();
        while (current != null) {
            if (current != items.getHead()) {
                text.append(", ");
            }
            text.append(current.getData());
            current = current.getNext();
        }
        return text.toString();
    }

    /**
     * Formats an amount of money with exactly two decimals, for example "EUR 240.00".
     */
    public static String euro(double amount) {
        long cents = Math.round(amount * 100);
        long whole = cents / 100;
        long rest = Math.abs(cents % 100);
        return "EUR " + whole + "." + (rest < 10 ? "0" : "") + rest;
    }

    /** @return the text, or "-" if it is null or blank (used for optional guest details) */
    public static String orDash(String text) {
        return (text == null || text.isBlank()) ? "-" : text;
    }
}
