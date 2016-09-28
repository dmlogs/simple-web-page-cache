package com.dmlogs.examples.services;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigurationServiceTest {

    private ConfigurationService configurationService;

    @Before
    public void setup() {
        configurationService = new ConfigurationService();
    }

    @Test
    public void testGetUpdateCacheIntervalGreaterThanZero() {
        configurationService.setUpdateCacheInterval(10);
        assertEquals(10, configurationService.getUpdateCacheInterval());
    }

    @Test
    public void testGetUpdateCacheIntervalDefault() {
        configurationService.setUpdateCacheInterval(0);
        assertEquals(30000, configurationService.getUpdateCacheInterval());
    }


    @Test
    public void testConfigureNoArgs() {
        configurationService.configure(new String[0]);
    }

    @Test
    public void testConfigureArgWithoutDeliminator() {
        configurationService.configure(new String[] { "thishasnodeliminator" });
    }

    @Test
    public void testConfigureMaxCacheSize() {
        configurationService.configure(new String[] { "maxCacheSize=10" });
        assertEquals(10,configurationService.getMaxCacheSize());
    }

    @Test
    public void testConfigureTtl() {
        configurationService.configure(new String[] { "ttl=100" });
        assertEquals(100,configurationService.getTTL());
    }

    @Test
    public void testConfigureCacheInterval() {
        configurationService.configure(new String[] { "updateCacheInterval=5" });
        assertEquals(5,configurationService.getUpdateCacheInterval());
    }

    @Test
    public void testConfigurePort() {
        configurationService.configure(new String[] { "port=8888"});
        assertEquals(8888,configurationService.getPort());
    }
}
