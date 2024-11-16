package edu.smu.smusql;

import java.util.Arrays;

public class DynamicArray<E> {
    private Object[] data;  // Array to store elements
    private int size = 0;   // Number of elements currently in the array
    private int capacity;   // Current capacity of the array

    public DynamicArray() {
        this.capacity = 10; // Initial capacity
        this.data = new Object[capacity];
    }

    // Returns the number of elements in the dynamic array
    public int size() {
        return size;
    }

    // Checks if the dynamic array is empty
    public boolean isEmpty() {
        return size == 0;
    }

    // Returns the element at a specific index
    @SuppressWarnings("unchecked")
    public E get(int index) {
        checkIndex(index); // Ensure index is within bounds
        return (E) data[index];
    }

    // Sets the element at a specific index and returns the old element
    @SuppressWarnings("unchecked")
    public E set(int index, E element) {
        checkIndex(index); // Ensure index is within bounds
        E oldElement = (E) data[index];
        data[index] = element;
        return oldElement;
    }

    // Adds an element to the end of the dynamic array
    public void add(E element) {
        if (size == capacity) {
            resize(); // Resize the array if capacity is reached
        }
        data[size++] = element;
    }

    // Inserts an element at a specific index
    public void add(int index, E element) {
        checkIndexForAdd(index); // Allow index == size for insertion at the end
        if (size == capacity) {
            resize();
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        data[index] = element;
        size++;
    }

    // Removes an element from a specific index
    @SuppressWarnings("unchecked")
    public E remove(int index) {
        checkIndex(index); // Ensure index is within bounds
        E element = (E) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[--size] = null; // Clear the last element
        return element;
    }

    // Removes the last element and returns it
    @SuppressWarnings("unchecked")
    public E removeLast() {
        if (isEmpty()) {
            return null;
        }
        E lastElement = (E) data[size - 1];
        data[--size] = null;
        return lastElement;
    }

    // Removes the first element and returns it
    @SuppressWarnings("unchecked")
    public E removeFirst() {
        return remove(0);
    }

    // Returns the first element without removing it
    @SuppressWarnings("unchecked")
    public E first() {
        return isEmpty() ? null : (E) data[0];
    }

    // Returns the last element without removing it
    @SuppressWarnings("unchecked")
    public E last() {
        return isEmpty() ? null : (E) data[size - 1];
    }

    // Converts the dynamic array to a string representation
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append(data[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // Private helper method to resize the internal array
    private void resize() {
        capacity *= 3;
        data = Arrays.copyOf(data, capacity);
    }

    // Helper method to check if an index is within bounds for get/set/remove
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
    }

    // Helper method to check if an index is within bounds for add
    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index out of bounds for add: " + index);
        }
    }
}
