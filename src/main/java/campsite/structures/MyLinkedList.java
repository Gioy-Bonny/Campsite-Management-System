package campsite.structures;

/**
 * A custom singly linked list, built from scratch (no java.util collections).
 * Keeps a head and a tail reference so adding to the end is fast.
 *
 * @param <T> the type of elements stored in the list
 */
public class MyLinkedList<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /** Creates an empty list. */
    public MyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Adds an item to the end of the list.
     *
     * @param item the item to add
     */
    public void add(T data){
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            // Empty list: the new node is both head and tail
            head = newNode;
            tail = newNode;
        }
        else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    /**
     * Returns the item at the given position (0 = first item).
     *
     * @param index position of the item
     * @return the item at that position
     * @throws IndexOutOfBoundsException if the index is not valid
     */
    public T get(int index){
        if (index < 0 || index >= size){
            throw new IndexOutOfBoundsException("Index "+ index + ", size "+ size );
        }
        Node<T> node = head;
        for (int i = 0; i < index; i++){
            node = node.getNext();
        }
        return node.getData();
    }

    /**
     * Removes the first occurrence of an item (compared with equals).
     *
     * @param item the item to remove
     * @return true if something was removed, false if the item was not found
     */
    public boolean remove(T item) {
        Node<T> prev = null;
        Node<T> current = head;

        while (current != null) {
            if (current.getData().equals(item)) {
                if (prev == null) {
                    //Remove the head
                    head = current.getNext();
                } else {
                    prev.setNext(current.getNext());
                }
                if (current == tail) {
                    tail = prev;
                }
                size--;
                return true;
            }
            prev = current;
            current = current.getNext();
        }
        return false;
    }

    /**
     * Checks whether the list contains the item (compared with equals).
     *
     * @param item the item to look for
     * @return true if found
     */
    public boolean contains(T item) {
        Node<T> node = head;
        while (node != null) {
            if (node.getData().equals(item)) {
                return true;
            }
            node = node.getNext();
        }
        return false;
    }

    /** @return the number of items in the list */
    public int size() {
        return size;
    }

    /** @return true if the list has no items */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all items from the list. */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Returns the first node, so other classes can walk the list:
     * <pre>
     * Node&lt;T&gt; n = list.getHead();
     * while (n != null) { ...n.getData()...; n = n.getNext(); }
     * </pre>
     *
     * @return the head node, or null if the list is empty
     */
    public Node<T> getHead() {
        return head;
    }
}
