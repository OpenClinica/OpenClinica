package org.akaza.openclinica.dao.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EhCacheWrapper<K, V> implements CacheWrapper<K, V> 
{
    private final String cacheName;
    private final CacheManager cacheManager;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    public EhCacheWrapper(final String cacheName, final CacheManager cacheManager)
    {
    this.cacheName = cacheName;
    this.cacheManager = cacheManager;
    }
    
    public void put(final K key, final V value)
    {
    	getCache().put(new Element(key, value));
    }
    
    public V get(final K key) 
    {
    	String db_type = CoreResources.getField("dbType");
    	if ( db_type.equalsIgnoreCase("postgres")){
	    	Element element =null;
		        Ehcache ehCache = getCache();
		        if(ehCache!=null)          
		        {
		            element = getCache().get(key);
		            logMe("element  null"+element);
		        }
		    if (element != null) {
		        logMe("element not null"+element);
		        return (V) element.getObjectValue();
		    }
    	}
    	return null;
    }
    public Ehcache getCache() 
    {
    	return cacheManager.getEhcache(cacheName);
    }

    
    private void logMe(String message){
        
        logger.debug(message);
       
    }
}

