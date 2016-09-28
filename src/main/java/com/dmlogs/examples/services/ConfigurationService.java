package com.dmlogs.examples.services;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationService {
    private static ConfigurationService instance = null;

    public static ConfigurationService getInstance() {
        if(instance == null) {
            // Only want lock for lazy instantiation
            synchronized(ConfigurationService.class) {
                if(instance == null) {
                    instance = new ConfigurationService();
                }
            }
        }

        return instance;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public void setTTL(long TTL) {
        this.TTL = TTL;
    }

    // 0 means cache size is infinite
    private int maxCacheSize = 0;

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    // 0 means TTL is infinite
    private long TTL = 0;

    public long getTTL() {
        return TTL;
    }

    public void setUpdateCacheInterval(long updateCacheInterval) {
        this.updateCacheInterval = updateCacheInterval;
    }

    /**
     * Interval to update cache and remove expired items.
     * Value is stored in milliseconds.
     */
    private long updateCacheInterval = 30000;

    /**
     * Asserts updateCatchInterval is greater than 0 otherwise it defaults to 30 seconds.
     *
     * @return Interval in milliseconds to check for expired items in the cache.
     */
    public long getUpdateCacheInterval() {
        if (updateCacheInterval > 0) {
            return updateCacheInterval;
        }

        return 30000;
    }

    private Logger logger = Logger.getLogger(ConfigurationService.class.getSimpleName());

    public void configure(String[] args) throws NumberFormatException {
        for(String arg : args) {
            if(arg.contains("=")) {
                String[] values = arg.split("=");

                switch(values[0]) {
                    case "maxCacheSize":
                        setMaxCacheSize(Integer.parseInt(values[1]));
                        logger.log(Level.INFO, "maxCacheSize set to " + values[1]);
                        break;
                    case "ttl":
                        setTTL(Long.parseLong(values[1]));
                        logger.log(Level.INFO, "ttl set to " + values[1]);
                        break;
                    case "updateCacheInterval":
                        setUpdateCacheInterval(Long.parseLong(values[1]));
                        logger.log(Level.INFO, "updateCacheInterval set to " + values[1]);
                        break;
                    default: break;
                }
            }
        }
    }

}
