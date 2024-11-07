package edu.smu.smusql;
/*
 * An implementation of SinglyLinkedList for the sample implementation of smuSQL.
 */
public class SinglyLinkedList<E> {

    private static class Node<E> {
        private E element;
        private Node<E> next;

        public Node(E e, Node<E> n){
            element = e;
            next = n;
        }

        public E getElement(){
            return element;
        }

        public Node<E> getNext(){
            return next;
        }

        public void setNext(Node<E> n){
            next = n;
        }
    }
    private Node<E> head = null;
    private Node<E> tail = null;
    private int size = 0;

    public SinglyLinkedList(){

    }

    public int size(){
        return size;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public E first(){
        if (isEmpty()){
            return null;
        }
        return head.getElement();
    }

    public E last(){
        if (isEmpty()){
            return null;
        }
        return tail.getElement();
    }

    public void addFirst(E e){
        head = new Node<>(e, head);

        if (isEmpty()){
            tail = head;
        }
        size++;
    }

    public E get(int index){
        Node<E> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }

        return current.getElement();
    }

    public void addLast(E e){
        Node<E> newest = new Node<>(e, null);
        if (isEmpty()){
            head = newest;
        } else {
            tail.setNext(newest);
        }
        tail = newest;
        size++;
    }

    public E removeFirst(){
        if (isEmpty()){
            return null;
        }

        E answer = head.getElement();
        head = head.getNext();
        size--;

        if (isEmpty()){
            tail = null;
        }
        return answer;
    }

    /*************************************/
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Node<E> current = head;
        while (current != null) {
            sb.append(current.getElement());
            sb.append(" ");
            current = current.getNext();
        }
        sb.append("]");
        return sb.toString();
    }

    public E removeLast(){
        if (isEmpty()){
            return null;
        }

        Node<E> last = tail;

        if (head == tail){
            head = null;
            tail = null;
            size--;
            return last.getElement();
        }

        Node<E> current = head;
        while (current.getNext() != tail){
            current = current.getNext();
        }
        current.setNext(null);
        tail = current;
        size--;
        return last.getElement();
    }

    public E remove(int index){
        if (index == 0){
            return removeFirst();
        }

        Node<E> current = head;
        for (int i = 1; i < index; i++) {
            current = current.getNext();
        }

        Node<E> toRemove = current.getNext();
        if (tail == toRemove){
            return removeLast();
        } else {
            current.setNext(toRemove.getNext());
            size--;
            return toRemove.getElement();
        }
    }
}