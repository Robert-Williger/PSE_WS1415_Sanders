package util;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.function.Consumer;

public class IntMap<V> {
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int TREEIFY_THRESHOLD = 8;
    static final int UNTREEIFY_THRESHOLD = 6;
    static final int MIN_TREEIFY_CAPACITY = 64;

    static class Node<V> {
        final int hash;
        final int key;
        V value;
        Node<V> next;

        Node(int hash, int key, V value, Node<V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final int getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return key ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Node) {
                Node<?> e = (Node<?>) o;
                if (key == e.getKey() && Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    static final int hash(final int key) {
        return key ^ (key >>> 16);
    }

    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType)
                            && ((p = (ParameterizedType) t).getRawType() == Comparable.class)
                            && (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c) // type arg is
                                                                                                          // c
                        return c;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" }) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 : ((Comparable) k).compareTo(x));
    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    transient Set<Integer> keySet;
    transient Collection<V> values;
    transient Node<V>[] table;
    transient Set<Node<V>> entrySet;
    transient int size;
    transient int modCount;
    int threshold;
    final float loadFactor;

    public IntMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    public IntMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public IntMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V get(final int key) {
        Node<V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * Implements Map.get and related methods
     *
     * @param hash
     *            hash for key
     * @param key
     *            the key
     * @return the node, or null if none
     */
    final Node<V> getNode(final int hash, final int key) {
        Node<V>[] tab;
        Node<V> first, e;
        int n;
        int k;
        if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // always check first node
                    ((k = first.key) == key || key == k))
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<V>) first).getTreeNode(hash, key);
                do {
                    if (e.hash == hash && ((k = e.key) == key || key == k))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     *
     * @param key
     *            The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(int key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously contained a mapping for
     * the key, the old value is replaced.
     *
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for
     *         <tt>key</tt>. (A <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt>
     *         with <tt>key</tt>.)
     */
    public V put(int key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * Implements Map.put and related methods
     *
     * @param hash
     *            hash for key
     * @param key
     *            the key
     * @param value
     *            the value to put
     * @param onlyIfAbsent
     *            if true, don't change existing value
     * @param evict
     *            if false, the table is in creation mode.
     * @return previous value, or null if none
     */
    final V putVal(int hash, int key, V value, boolean onlyIfAbsent, boolean evict) {
        Node<V>[] tab;
        Node<V> p;
        int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<V> e;
            int k;
            if (p.hash == hash && ((k = p.key) == key || key == k))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<V>) p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0;; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash && ((k = e.key) == key || key == k))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }

    /**
     * Initializes or doubles table size. If null, allocates in accord with initial capacity target held in field
     * threshold. Otherwise, because we are using power-of-two expansion, the elements from each bin must either stay at
     * same index, or move with a power of two offset in the new table.
     *
     * @return the table
     */
    final Node<V>[] resize() {
        Node<V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        } else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else { // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({ "unchecked" })
        Node<V>[] newTab = (Node<V>[]) new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<V>) e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<V> loHead = null, loTail = null;
                        Node<V> hiHead = null, hiTail = null;
                        Node<V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            } else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * Replaces all linked nodes in bin at index for given hash unless table is too small, in which case resizes
     * instead.
     */
    final void treeifyBin(Node<V>[] tab, int hash) {
        int n, index;
        Node<V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<V> hd = null, tl = null;
            do {
                TreeNode<V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key
     *            key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for
     *         <tt>key</tt>. (A <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt>
     *         with <tt>key</tt>.)
     */
    public V remove(int key) {
        Node<V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ? null : e.value;
    }

    /**
     * Implements Map.remove and related methods
     *
     * @param hash
     *            hash for key
     * @param key
     *            the key
     * @param value
     *            the value to match if matchValue, else ignored
     * @param matchValue
     *            if true only remove if value is equal
     * @param movable
     *            if false do not move other nodes while removing
     * @return the node, or null if none
     */
    final Node<V> removeNode(int hash, int key, Object value, boolean matchValue, boolean movable) {
        Node<V>[] tab;
        Node<V> p;
        int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = (n - 1) & hash]) != null) {
            Node<V> node = null, e;
            int k;
            V v;
            if (p.hash == hash && ((k = p.key) == key || key == k))
                node = p;
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode)
                    node = ((TreeNode<V>) p).getTreeNode(hash, key);
                else {
                    do {
                        if (e.hash == hash && ((k = e.key) == key || key == k)) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    ((TreeNode<V>) node).removeTreeNode(this, tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    public void clear() {
        Node<V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     *
     * @param value
     *            value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     */
    public boolean containsValue(Object value) {
        Node<V>[] tab;
        V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value || (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    public Set<Integer> keySet() {
        Set<Integer> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    final class KeySet extends AbstractSet<Integer> {
        public final int size() {
            return size;
        }

        public final void clear() {
            IntMap.this.clear();
        }

        public final Iterator<Integer> iterator() {
            return new KeyIterator();
        }

        public final boolean contains(Integer o) {
            return containsKey(o);
        }

        public final boolean remove(Integer key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }

        public final void forEach(Consumer<? super Integer> action) {
            Node<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice-versa. If the map is modified while an iteration
     * over the collection is in progress (except through the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined. The collection supports element removal, which removes the corresponding mapping
     * from the map, via the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size() {
            return size;
        }

        public final void clear() {
            IntMap.this.clear();
        }

        public final Iterator<V> iterator() {
            return new ValueIterator();
        }

        public final boolean contains(Object o) {
            return containsValue(o);
        }

        public final void forEach(Consumer<? super V> action) {
            Node<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the map, so changes to the
     * map are reflected in the set, and vice-versa. If the map is modified while an iteration over the set is in
     * progress (except through the iterator's own <tt>remove</tt> operation, or through the <tt>setValue</tt> operation
     * on a map entry returned by the iterator) the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support
     * the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Node<V>> entrySet() {
        Set<Node<V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Node<V>> {
        public final int size() {
            return size;
        }

        public final void clear() {
            IntMap.this.clear();
        }

        public final Iterator<Node<V>> iterator() {
            return new EntryIterator();
        }

        public final boolean contains(Object o) {
            if (!(o instanceof Node))
                return false;
            Node<?> e = (Node<?>) o;
            int key = e.getKey();
            Node<V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }

        public final boolean remove(Object o) {
            if (o instanceof Node) {
                Node<?> e = (Node<?>) o;
                int key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }

        public final void forEach(Consumer<? super Node<V>> action) {
            Node<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // Overrides of JDK8 Map extension methods

    public V getOrDefault(int key, V defaultValue) {
        Node<V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    public V putIfAbsent(int key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    public boolean remove(int key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    public boolean replace(int key, V oldValue, V newValue) {
        Node<V> e;
        V v;
        if ((e = getNode(hash(key), key)) != null && ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    public V replace(int key, V value) {
        Node<V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    // These methods are also used when serializing HashSets
    final float loadFactor() {
        return loadFactor;
    }

    final int capacity() {
        return (table != null) ? table.length : (threshold > 0) ? threshold : DEFAULT_INITIAL_CAPACITY;
    }

    /* ------------------------------------------------------------ */
    // iterators

    abstract class HashIterator {
        Node<V> next; // next entry to return
        Node<V> current; // current entry
        int expectedModCount; // for fast-fail
        int index; // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<V> nextNode() {
            Node<V>[] t;
            Node<V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            int key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator implements PrimitiveIterator.OfInt {
        public final int nextInt() {
            return nextNode().key;
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V> {
        public final V next() {
            return nextNode().value;
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Node<V>> {
        public final Node<V> next() {
            return nextNode();
        }
    }

    static class HashMapSpliterator<V> {
        final IntMap<V> map;
        Node<V> current; // current node
        int index; // current index, modified on advance/split
        int fence; // one past last index
        int est; // size estimate
        int expectedModCount; // for comodification checks

        HashMapSpliterator(IntMap<V> m, int origin, int fence, int est, int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                IntMap<V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    Node<V> newNode(int hash, int key, V value, Node<V> next) {
        return new Node<>(hash, key, value, next);
    }

    // For conversion from TreeNodes to plain nodes
    Node<V> replacementNode(Node<V> p, Node<V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // Create a tree bin node
    TreeNode<V> newTreeNode(int hash, int key, V value, Node<V> next) {
        return new TreeNode<V>(hash, key, value, next);
    }

    // For treeifyBin
    TreeNode<V> replacementTreeNode(Node<V> p, Node<V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * Reset to initial default state. Called by clone and readObject.
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<V> p) {
    }

    void afterNodeInsertion(boolean evict) {
    }

    void afterNodeRemoval(Node<V> p) {
    }

    // Called only from writeObject, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn extends Node) so can be used as extension of
     * either regular or linked node.
     */
    static final class TreeNode<V> extends Node<V> {
        TreeNode<V> before, after;
        TreeNode<V> parent; // red-black tree links
        TreeNode<V> left;
        TreeNode<V> right;
        TreeNode<V> prev; // needed to unlink next upon deletion
        boolean red;

        TreeNode(int hash, int key, V val, Node<V> next) {
            super(hash, key, val, next);
        }

        /**
         * Returns root of tree containing this node.
         */
        final TreeNode<V> root() {
            for (TreeNode<V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * Ensures that the given root is the first node of its bin.
         */
        static <K, V> void moveRootToFront(Node<V>[] tab, TreeNode<V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<V> first = (TreeNode<V>) tab[index];
                if (root != first) {
                    Node<V> rn;
                    tab[index] = root;
                    TreeNode<V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<V>) rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }

        /**
         * Finds the node starting at root p with the given hash and key. The kc argument caches comparableClassFor(key)
         * upon first use comparing keys.
         */
        final TreeNode<V> find(int h, int k, Class<?> kc) {
            TreeNode<V> p = this;
            do {
                int ph, dir;
                int pk;
                TreeNode<V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || k == pk)
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null || (kc = comparableClassFor(k)) != null)
                        && (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }

        /**
         * Calls find for root node.
         */
        final TreeNode<V> getTreeNode(int h, int k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * Tie-breaking utility for ordering insertions when equal hashCodes and non-comparable. We don't require a
         * total order, just a consistent insertion rule to maintain equivalence across rebalancings. Tie-breaking
         * further than necessary simplifies testing a bit.
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1);
            return d;
        }

        /**
         * Forms tree of the nodes linked from this node.
         * 
         * @return root of tree
         */
        final void treeify(Node<V>[] tab) {
            TreeNode<V> root = null;
            for (TreeNode<V> x = this, next; x != null; x = next) {
                next = (TreeNode<V>) x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else {
                    int k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<V> p = root;;) {
                        int dir, ph;
                        int pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null && (kc = comparableClassFor(k)) == null)
                                || (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from this node.
         */
        final Node<V> untreeify(IntMap<V> map) {
            Node<V> hd = null, tl = null;
            for (Node<V> q = this; q != null; q = q.next) {
                Node<V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * Tree version of putVal.
         */
        final TreeNode<V> putTreeVal(IntMap<V> map, Node<V>[] tab, int h, int k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<V> root = (parent != null) ? root() : this;
            for (TreeNode<V> p = root;;) {
                int dir, ph;
                int pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || k == pk)
                    return p;
                else if ((kc == null && (kc = comparableClassFor(k)) == null)
                        || (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null && (q = ch.find(h, k, kc)) != null)
                                || ((ch = p.right) != null && (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<V> xpn = xp.next;
                    TreeNode<V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<V>) xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * Removes the given node, that must be present before this call. This is messier than typical red-black
         * deletion code because we cannot swap the contents of an interior node with a leaf successor that is pinned by
         * "next" pointers that are accessible independently during traversal. So instead we swap the tree linkages. If
         * the current tree appears to have too few nodes, the bin is converted back to a plain bin. (The test triggers
         * somewhere between 2 and 6 nodes, depending on tree structure).
         */
        final void removeTreeNode(IntMap<V> map, Node<V>[] tab, boolean movable) {
            int n;
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;
            TreeNode<V> first = (TreeNode<V>) tab[index], root = first, rl;
            TreeNode<V> succ = (TreeNode<V>) next, pred = prev;
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (root == null || root.right == null || (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map); // too small
                return;
            }
            TreeNode<V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red;
                s.red = p.red;
                p.red = c; // swap colors
                TreeNode<V> sr = s.right;
                TreeNode<V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                } else {
                    TreeNode<V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            } else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) { // detach
                TreeNode<V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins, or untreeifies if now too small. Called only from
         * resize; see above discussion about split bits and indices.
         *
         * @param map
         *            the map
         * @param tab
         *            the table for recording bin heads
         * @param index
         *            the index of the table being split
         * @param bit
         *            the bit of hash to split on
         */
        final void split(IntMap<V> map, Node<V>[] tab, int index, int bit) {
            TreeNode<V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<V> loHead = null, loTail = null;
            TreeNode<V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<V> e = b, next; e != null; e = next) {
                next = (TreeNode<V>) e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                } else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K, V> TreeNode<V> rotateLeft(TreeNode<V> root, TreeNode<V> p) {
            TreeNode<V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K, V> TreeNode<V> rotateRight(TreeNode<V> root, TreeNode<V> p) {
            TreeNode<V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K, V> TreeNode<V> balanceInsertion(TreeNode<V> root, TreeNode<V> x) {
            x.red = true;
            for (TreeNode<V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                } else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K, V> TreeNode<V> balanceDeletion(TreeNode<V> root, TreeNode<V> x) {
            for (TreeNode<V> xp, xpl, xpr;;) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (x.red) {
                    x.red = false;
                    return root;
                } else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        } else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ? null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                } else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        } else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ? null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        static <K, V> boolean checkInvariants(TreeNode<V> t) {
            TreeNode<V> tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode<V>) t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}
