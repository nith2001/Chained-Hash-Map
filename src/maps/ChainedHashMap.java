package maps;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @see AbstractIterableMap
 * @see Map
 */
@SuppressWarnings("checkstyle:CommentsIndentation")
public class ChainedHashMap<K, V> extends AbstractIterableMap<K, V> {
    private static final double DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD = 0.5;
    private static final int DEFAULT_INITIAL_CHAIN_COUNT = 12;
    private static final int DEFAULT_INITIAL_CHAIN_CAPACITY = 8;

    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
    */
    AbstractIterableMap<K, V>[] chains;
    private double resizeFactor;
    private int size;
    private int chainSize;
    // You're encouraged to add extra fields (and helper methods) though!

    /**
     * Constructs a new ChainedHashMap with default resizing load factor threshold,
     * default initial chain count, and default initial chain capacity.
     */
    public ChainedHashMap() {
        this(DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD, DEFAULT_INITIAL_CHAIN_COUNT, DEFAULT_INITIAL_CHAIN_CAPACITY);
    }

    /**
     * Constructs a new ChainedHashMap with the given parameters.
     *
     * @param resizingLoadFactorThreshold the load factor threshold for resizing. When the load factor
     *                                    exceeds this value, the hash table resizes. Must be > 0.
     * @param initialChainCount the initial number of chains for your hash table. Must be > 0.
     * @param chainInitialCapacity the initial capacity of each ArrayMap chain created by the map.
     *                             Must be > 0.
     */
    public ChainedHashMap(double resizingLoadFactorThreshold, int initialChainCount, int chainInitialCapacity) {
        this.chains = this.createArrayOfChains(initialChainCount);
        chainSize = chainInitialCapacity;
        resizeFactor = resizingLoadFactorThreshold;
        size = 0;
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code AbstractIterableMap<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     * @see ArrayMap createArrayOfEntries method for more background on why we need this method
     */
    @SuppressWarnings("unchecked")
    private AbstractIterableMap<K, V>[] createArrayOfChains(int arraySize) {
        return (AbstractIterableMap<K, V>[]) new AbstractIterableMap[arraySize];
    }

    /**
     * Returns a new chain.
     *
     * This method will be overridden by the grader so that your ChainedHashMap implementation
     * is graded using our solution ArrayMaps.
     *
     * Note: You do not need to modify this method.
     */
    protected AbstractIterableMap<K, V> createChain(int initialSize) {
        return new ArrayMap<>(initialSize);
    }

    @Override
    public V get(Object key) {
        int index = getIndex(key);
        if (chains[index] != null) {
            return chains[index].get(key);
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        if (((double) size) / chains.length > resizeFactor) {
            resize();
        }
        int index = getIndex(key);
        if (chains[index] == null) {
            chains[index] = createChain(chainSize);
        }
        if (!chains[index].containsKey(key)) {
            size++;
        }
        return chains[index].put(key, value);
    }

    @Override
    public V remove(Object key) {
        int index = getIndex(key);
        if (chains[index] == null) {
            return null;
        } else {
            size--;
            return chains[index].remove(key);
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < chains.length; i++) {
            if (chains[i] != null) {
                chains[i].clear();
            }
        }
        size = 0;
    }


    @Override
    public boolean containsKey(Object key) {
        int index = getIndex(key);
        if (chains[index] == null) {
            return false;
        }
        return chains[index].containsKey(key);
    }

    @Override
    public int size() {
        return size;
    }

    private int getIndex(Object key) {
        int index = 0;
        if (key != null) {
            index = Math.abs(key.hashCode()) % chains.length;
        }
        return index;
    }

    private void resize() {
        AbstractIterableMap<K, V>[] newChains = createArrayOfChains(chains.length * 2);
        for (Entry<K, V> pair : this) {
            int index = 0;
            K key = pair.getKey();
            if (key != null) {
                index = Math.abs(key.hashCode()) % newChains.length;
            }
            if (newChains[index] == null) {
                newChains[index] = createChain(chainSize);
            }
            newChains[index].put(key, pair.getValue());
        }
        this.chains = newChains;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: you won't need to change this method (unless you add more constructor parameters)
        return new ChainedHashMapIterator<>(this.chains, this.size);
    }

    /*
    See the assignment webpage for tips and restrictions on implementing this iterator.
     */
    private static class ChainedHashMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private AbstractIterableMap<K, V>[] chains;
        private Iterator<Map.Entry<K, V>> iterator;
        private int currentChain;
        private int counter;
        // You may add more fields and constructor parameters

        public ChainedHashMapIterator(AbstractIterableMap<K, V>[] chains, int size) {
            this.chains = chains;
            this.currentChain = 0;
            this.counter = size;

            while (currentChain < chains.length) {
                if (chains[currentChain] != null) {
                    this.iterator = chains[currentChain].iterator();
                    break;
                }
                currentChain++;
            }
        }

        @Override
        public boolean hasNext() {
            return counter > 0;
        }

        @Override
        public Map.Entry<K, V> next() {
            if (iterator != null && iterator.hasNext()) {
                counter--;
                return iterator.next();
            }
            while (currentChain < chains.length - 1) {
                currentChain++;
                if (chains[currentChain] != null) {
                    iterator = chains[currentChain].iterator();
                    if (iterator.hasNext()) {
                        counter--;
                        return iterator.next();
                    }
                }
            }
            throw new NoSuchElementException();
        }
    }
}
