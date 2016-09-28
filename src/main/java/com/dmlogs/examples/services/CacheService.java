package com.dmlogs.examples.services;

import com.dmlogs.examples.models.CacheItemModel;
import org.apache.commons.collections4.map.LinkedMap;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheService<T extends CacheItemModel> {

    private static CacheService instance = null;

    public static CacheService getInstance() {
        if (instance == null) {
            // Only want lock for lazy instantiation
            synchronized (CacheService.class) {
                if (instance == null) {
                    instance = new CacheService();
                }
            }
        }

        return instance;
    }

    private ConfigurationService config = ConfigurationService.getInstance();
    private int hitCount = 0;
    private int requestCount = 0;
    private LinkedMap<UUID, T> collection = new LinkedMap<>();

    private Thread thread = null;
    private CacheCleaner cleaner = null;

    public class CacheCleaner implements Runnable {
        private boolean keepRunning = true;

        @Override
        public void run() {
            while (keepRunning) {
                try {
                    Thread.sleep(config.getUpdateCacheInterval());
                } catch (InterruptedException ie) {
                    Logger.getLogger("CacheServiceThread").log(Level.WARNING, "Thread was interrupted.");
                }

                checkForExpiredItems();
            }
        }

        public void terminate() {
            keepRunning = false;
        }
    }

    public CacheService() {
        assertCacheSize = config.getMaxCacheSize() > 0;

        if (config.getTTL() > 0) {
            cleaner = new CacheCleaner();
            thread = new Thread(cleaner);
            thread.start();
        }
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public double getHitRate() {
        if (hitCount > requestCount || requestCount == 0) {
            return 1.00;
        }

        return (double) hitCount / (double) requestCount;
    }

    public synchronized void add(T item) {
        collection.put(item.getId(), item);
        assertCacheSize();
    }

    private boolean assertCacheSize;

    private synchronized void assertCacheSize() {
        if (assertCacheSize) {
            while (collection.size() > config.getMaxCacheSize()) {
                removeFirst();
            }
        }
    }

    private synchronized void checkForExpiredItems() {
        while (firstItemIsExpired()) {
            removeFirst();
        }
    }

    private synchronized boolean firstItemIsExpired() {
        if (collection.size() > 0) {
            return config.getTTL() > 0 &&
                    config.getTTL() < System.currentTimeMillis() - collection.get(collection.firstKey()).getCreatedAt();
        }
        else {
            return false;
        }
    }

    private synchronized void removeFirst() {
        if (collection.size() > 0) {
            collection.remove(collection.firstKey());
        }
    }

    /**
     * This method retrieves an item from the cache if
     * it exists based on the provided unique identifier.
     * <p>
     * Note: At this time not implementing an update
     * to TTL/cache position on item access.
     *
     * @param key Unique identifier for the item being retrieved.
     * @return The corresponding item in the cache if it exists, else null.
     */
    public synchronized T get(UUID key) {
        requestCount++;

        if (collection.containsKey(key)) {
            hitCount++;
            return collection.get(key);
        }

        return null;
    }
}
