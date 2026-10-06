package campsite.structures;

/**
 * A single node of a singly linked list.
 * It stores one piece of data and a reference to the next node.
 *
 * @param <T> the type of data stored in the node
 */

public class Node<T> {

    private T data;
    private Node<T> next;

    /**
     * Creates a node holding the given data, not yet linked to anything.
     *
     * @param data the value to store
     */
    public Node(T data) {
        this.data = data;
        this.next = null;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public Node<T> getNext() {
        return next;
    }
    public void setNext(Node<T> next) {
        this.next = next;
    }

}
