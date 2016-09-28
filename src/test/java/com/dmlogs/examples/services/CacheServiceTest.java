package com.dmlogs.examples.services;

import com.dmlogs.examples.models.CacheItemModel;
import org.apache.commons.collections4.map.LinkedMap;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CacheServiceTest {

    private CacheService<CacheItemModel> cacheService = null;

    public void initialize() {
        cacheService = new CacheService<>();
    }

    /**
     * Test adding an item to the cache.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void testAddItem() throws NoSuchFieldException, IllegalAccessException {
        initialize();

        CacheItemModel cacheItemModel = new CacheItemModel();
        cacheService.add(cacheItemModel);

        LinkedMap<UUID, CacheItemModel> collection = getCollectionFromCacheService();

        assertTrue(collection.containsKey(cacheItemModel.getId()));
        assertEquals(cacheItemModel, collection.get(cacheItemModel.getId()));
    }

    private LinkedMap<UUID, CacheItemModel> getCollectionFromCacheService() throws NoSuchFieldException, IllegalAccessException {
        Field field =  CacheService.class.getDeclaredField("collection");
        field.setAccessible(true);
        return (LinkedMap<UUID, CacheItemModel>) field.get(cacheService);
    }

    /**
     * Test assertion items are not
     */
    @Test
    public void testAssertCacheSizeInfinite() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        initialize();

        testAssertCacheSizeField(false);

        for (int i = 0; i < 50; i++) {
            cacheService.add(new CacheItemModel());
        }

        assertEquals(50, getCollectionFromCacheService().size());
    }

    private void testAssertCacheSizeField(boolean expected) throws IllegalAccessException, NoSuchFieldException {
        Field assertCacheSize = CacheService.class.getDeclaredField("assertCacheSize");
        assertCacheSize.setAccessible(true);
        assertEquals(expected, assertCacheSize.getBoolean(cacheService));
    }

    public void resetMaxCacheSize() {
        setMaxCacheSize(0);
    }

    public void setMaxCacheSize(int maxCacheSize) {
        ConfigurationService.getInstance().setMaxCacheSize(maxCacheSize);
    }

    @Test
    public void testAssertCacheSizeFinite() throws NoSuchFieldException, IllegalAccessException {
        setMaxCacheSize(10);
        initialize();

        testAssertCacheSizeField(true);

        for (int i = 0; i < 50; i++) {
            cacheService.add(new CacheItemModel());
        }

        assertEquals(10, getCollectionFromCacheService().size());
        resetMaxCacheSize();
    }

    private boolean invokeFirstItemIsExpired() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method firstItemIsExpired = CacheService.class.getDeclaredMethod("firstItemIsExpired");
        firstItemIsExpired.setAccessible(true);
        return (boolean) firstItemIsExpired.invoke(cacheService);
    }

    @Test
    public void testFirstItemIsExpiredForEmptyCollection() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        initialize();
        assertFalse(invokeFirstItemIsExpired());
    }

    @Test
    public void testFirstItemIsExpiredForNonEmptyCollectionWithInfiniteTTL() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        initialize();
        cacheService.add(new CacheItemModel());
        assertFalse(invokeFirstItemIsExpired());
    }

    private void resetTTL() {
        setTTL(0);
    }

    private void setTTL(long ttl) {
        ConfigurationService.getInstance().setTTL(ttl);
    }

    /**
     * First item has not expired the TTL.
     */
    @Test
    public void testFirstItemIsExpiredForCollectionWithFirstItemAlive() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        setTTL(10000);
        initialize();

        cacheService.add(new CacheItemModel());
        assertFalse(invokeFirstItemIsExpired());

        resetTTL();
    }

    @Test
    public void testFirstItemIsExpiredForCollectionWithFirstItemExpired() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InterruptedException {
        setTTL(100);
        initialize();

        cacheService.add(new CacheItemModel());
        Thread.sleep(200);
        assertTrue(invokeFirstItemIsExpired());

        resetTTL();
    }


    public void invokeRemoveFirst() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method removeFirst = CacheService.class.getDeclaredMethod("removeFirst");
        removeFirst.setAccessible(true);
        removeFirst.invoke(cacheService);
    }

    @Test
    public void testRemoveFirstForEmptyCollection() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        initialize();
        invokeRemoveFirst();
    }

    @Test
    public void testRemoveFirstForNonEmptyCollection() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        initialize();
        cacheService.add(new CacheItemModel());
        LinkedMap<UUID, CacheItemModel> collection = getCollectionFromCacheService();
        assertEquals(1, collection.size());
        invokeRemoveFirst();
        assertEquals(0, collection.size());
    }

    private void invokeCheckForExpiredItems() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method checkForExpiredItems = CacheService.class.getDeclaredMethod("checkForExpiredItems");
        checkForExpiredItems.setAccessible(true);
        checkForExpiredItems.invoke(cacheService);
    }

    @Test
    public void testCheckForExpiredItemsFirstItemValid() throws InterruptedException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        setTTL(10000);
        initialize();

        cacheService.add(new CacheItemModel());
        invokeCheckForExpiredItems();
        LinkedMap<UUID, CacheItemModel> collection = getCollectionFromCacheService();
        assertEquals(1, collection.size());

        resetTTL();
    }

    @Test
    public void testCheckForExpiredItemsFirstItemExpired() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException, InterruptedException {
        setTTL(100);
        initialize();

        cacheService.add(new CacheItemModel());
        Thread.sleep(200);
        invokeCheckForExpiredItems();
        LinkedMap<UUID, CacheItemModel> collection = getCollectionFromCacheService();
        assertEquals(0, collection.size());

        resetTTL();
    }


    @Test
    public void testGetExistingItem() {
        initialize();

        CacheItemModel item = new CacheItemModel();
        cacheService.add(item);

        CacheItemModel result = cacheService.get(item.getId());

        assertEquals(item,result);
    }

    @Test
    public void testGetMissingItem() {
        initialize();

        CacheItemModel result = cacheService.get(UUID.randomUUID());

        assertEquals(null,result);
    }

    @Test
    public void testGetHitRate() {
        initialize();

        CacheItemModel cacheItemModel = new CacheItemModel();
        cacheService.add(cacheItemModel);

        for(int i = 0; i < 9; i++) {
            cacheService.get(cacheItemModel.getId());
        }

        cacheService.get(UUID.randomUUID());

        assertEquals(0.900,cacheService.getHitRate(),0);
    }

    public void resetUpdateCacheInterval() {
        setUpdateCacheInterval(30000);
    }

    public void setUpdateCacheInterval(int updateCacheInterval) {
        ConfigurationService.getInstance().setUpdateCacheInterval(updateCacheInterval);
    }

    /**
     * Test that the cache is properly cleaned over time.
     */
    @Test
    public void testCacheCleaner() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        setTTL(300);
        setUpdateCacheInterval(400);
        initialize();

        LinkedMap<UUID, CacheItemModel> collection = getCollectionFromCacheService();

        cacheService.add(new CacheItemModel());
        Thread.sleep(200);
        cacheService.add(new CacheItemModel());
        assertEquals(2, collection.size());
        Thread.sleep(250);
        // First item should have expired and been cleaned by now.
        assertEquals(1, collection.size());
        Thread.sleep(400);
        // Second item should have expired and been cleaned by now.
        assertEquals(0, collection.size());

        resetUpdateCacheInterval();
    }

    /**
     * If the cacheService is using the CacheCleaner we need to assert proper teardown and terminate the thread between tests.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws InterruptedException
     */
    @After
    public void cleanup() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        if (cacheService != null) {
            Field threadField = CacheService.class.getDeclaredField("thread");
            threadField.setAccessible(true);
            Thread thread = (Thread) threadField.get(cacheService);

            if (thread != null) {
                Field cleanerField = CacheService.class.getDeclaredField("cleaner");
                cleanerField.setAccessible(true);
                CacheService.CacheCleaner cleaner = (CacheService.CacheCleaner) cleanerField.get(cacheService);
                cleaner.terminate();
                thread.join();
            }
        }
    }
}
