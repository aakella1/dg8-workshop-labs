package org.acme;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

// Java JCache API
public class Exercise2 {

    public static void main(String[] args) {
        // TODO: Construct a simple local cache manager with default configuration
        CachingProvider jcacheProvider = Caching.getCachingProvider();
        CacheManager cacheManager = jcacheProvider.getCacheManager();
        MutableConfiguration<String, String> configuration = new MutableConfiguration<>();
        configuration.setTypes(String.class, String.class);

        // TODO: create a cache using the supplied configuration
        Cache<String, String> cache = cacheManager.createCache("myCache", configuration);

        // Store and retrieve value
        cache.put("key", "value");
        System.out.printf("key = %s\n", cache.get("key"));

        // TODO: Stop the cache manager and release all resources
        cacheManager.close();
    }

}