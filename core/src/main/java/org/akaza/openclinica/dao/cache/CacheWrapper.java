package org.akaza.openclinica.dao.cache;

public interface CacheWrapper<K, V> {
    
    V get(final K key);
    void put(final K key,final V value);

}
