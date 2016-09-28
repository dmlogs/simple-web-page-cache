package com.dmlogs.examples.resources;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.dmlogs.examples.Main;
import com.dmlogs.examples.models.GetResponseModel;
import com.dmlogs.examples.services.CacheService;
import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CacheResourceTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Test GET to cache to assert that the cache statistics page is properly populated for an empty cache.
     */
    @Test
    public void testGetCache() throws NoSuchFieldException, IllegalAccessException {
        String responseMsg = target.path("cache").request().get(String.class);

        CacheService cacheService = CacheService.getInstance();
        String cacheStatsTemplate = (String) getPrivateStaticField(CacheResource.class, "cacheStatsTemplate");
        String cacheStats = String.format(cacheStatsTemplate, cacheService.getHitCount(),cacheService.getRequestCount(),cacheService.getHitRate());

        assertEquals(cacheStats, responseMsg);
    }

    private Object getPrivateStaticField(Class c,String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = c.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    /**
     * Test POST to cache where url is null.
     */
    @Test
    public void testPostCacheUrlIsNull() throws NoSuchFieldException, IllegalAccessException {
        Response response = target.path("cache").request().post(null);

        String nullUrlMessage = (String) getPrivateStaticField(CacheResource.class, "nullUrlMessage");

        assertEquals(400, response.getStatus());
        assertEquals(nullUrlMessage, response.readEntity(String.class));
    }

    private void testPostCacheUrlIsInvalid(String url) throws NoSuchFieldException, IllegalAccessException {
        Response response = testPostToCache(url);

        String invalidUrlMessageTemplate = (String) getPrivateStaticField(CacheResource.class, "invalidUrlMessageTemplate");

        assertEquals(400, response.getStatus());
        assertEquals(String.format(invalidUrlMessageTemplate, url), response.readEntity(String.class));
    }

    /**
     * Test POST to cache where url is not a valid scheme (ftp).
     */
    @Test
    public void testPostCacheUrlIsInvalidSchemeFtp() throws NoSuchFieldException, IllegalAccessException {
        testPostCacheUrlIsInvalid("ftp://test:test@1.2.3.4:1234/test");
    }

    private Response testPostToCache(String url) {
        return target.path("cache")
                .queryParam("url",url)
                .request().post(null);
    }

    private void testPostCacheUrlIsValid(String url) throws NoSuchFieldException, IllegalAccessException {
        Response response = testPostToCache(url);

        assertEquals(200, response.getStatus());
        assertTrue(UUID.fromString(response.readEntity(String.class)) != null);
    }

    /**
     * Test POST to cache where url is valid scheme (http).
     */
    @Test
    public void testPostCacheUrlIsValidSchemeHttp() throws NoSuchFieldException, IllegalAccessException {
        testPostCacheUrlIsValid("http://www.google.com");
    }

    /**
     * Test POST to cache where url is valid scheme (https).
     */
    @Test
    public void testPostCacheUrlIsValidSchemeHttps() throws NoSuchFieldException, IllegalAccessException {
        testPostCacheUrlIsValid("https://www.google.com");
    }

    /**
     * Test POST to cache where url is missing scheme (invalid).
     */
    @Test
    public void testPostCacheUrlNoScheme() throws NoSuchFieldException, IllegalAccessException {
        testPostCacheUrlIsInvalid("www.google.com");
    }

    @Test
    public void testPostCacheUrlIsValidUnknownHost() throws NoSuchFieldException, IllegalAccessException {
        String url = "http://www.reallyhopethiswebpagestilldoesntexistbutmaybeitdoes.com";
        Response response = testPostToCache(url);

        String unknownHostMessageTemplate = (String) getPrivateStaticField(CacheResource.class, "unknownHostMessageTemplate");

        assertEquals(200, response.getStatus());
        assertEquals(String.format(unknownHostMessageTemplate,url), response.readEntity(String.class));
    }

    private Response testGetFromCache(String id) {
        return target.path(String.format("cache/%s",id)).request().get();
    }

    /**
     * Test GET item from cache with invalid UUID.
     */
    @Test
    public void testGetFromCacheForInvalidUuid() throws NoSuchFieldException, IllegalAccessException {
        String id = "not-a-uuid";
        Response response = testGetFromCache(id);

        String invalidUniqueIdentifierMessageTemplate = (String) getPrivateStaticField(CacheResource.class, "invalidUniqueIdentifierMessageTemplate");

        assertEquals(400,response.getStatus());
        assertEquals(String.format(invalidUniqueIdentifierMessageTemplate, id),response.readEntity(String.class));
    }

    /**
     * Test GET item from cache for UUID not found in cache.
     */
    @Test
    public void testGetFromCacheForMissingUuid() throws NoSuchFieldException, IllegalAccessException {
        String id = UUID.randomUUID().toString();
        Response response = testGetFromCache(id);

        String notFoundInCacheMessageTemplate = (String) getPrivateStaticField(CacheResource.class, "notFoundInCacheMessageTemplate");

        assertEquals(404,response.getStatus());
        assertEquals(String.format(notFoundInCacheMessageTemplate, id),response.readEntity(String.class));
    }

    /**
     * Test GET item from cache for UUID found in cache.
     */
    @Test
    public void testGetFromCacheForExistingUuid() {
        String id = testPostToCache("http://www.google.com").readEntity(String.class);
        Response response = testGetFromCache(id);

        CacheService<GetResponseModel> cache = CacheService.getInstance();
        GetResponseModel getResponseModel = cache.get(UUID.fromString(id));

        assertEquals(200, response.getStatus());
        assertEquals(getResponseModel.getBody(),response.readEntity(String.class));
        assertEquals(getResponseModel.getHeaders(), response.getHeaders());
    }
}
