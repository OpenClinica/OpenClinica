/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.cache;

public interface CacheWrapper<K, V> {
    
    V get(final K key);
    void put(final K key,final V value);

}
