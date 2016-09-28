package com.dmlogs.examples.models;

import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetResponseModelTest {

    @Test
    public void testGetResponse() {
        GetResponseModel getResponseModel = new GetResponseModel(new MultivaluedHashMap<String, Object>(), "test");
        Response result = getResponseModel.getResponse();

        assertEquals(200, result.getStatus());
        assertEquals(getResponseModel.getHeaders(), result.getHeaders());
        assertEquals(getResponseModel.getBody(), result.getEntity());
    }

    @Test
    public void testCreateSuccess() throws ProcessingException {
        GetResponseModel getResponseModel = GetResponseModel.create("http://www.google.com");
        assertTrue(getResponseModel.getBody().length() > 0);
    }

    @Test
    public void testCreateThrowsException() {
        try {
            GetResponseModel.create("http://www.reallyhopethiswebpagestilldoesntexistbutmaybeitdoes.com");
            assertTrue(false);
        }
        catch (ProcessingException pe) {
            assertTrue(true);
        }
    }
}
