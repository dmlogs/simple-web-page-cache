package com.dmlogs.examples.models;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Model for storing GET response in the cache.
 *
 */
public class GetResponseModel extends CacheItemModel {

    private MultivaluedMap<String,Object> headers;
    private String body;

    public GetResponseModel(MultivaluedMap<String, Object> responseHeaders, String responseBody) {
        this.headers = responseHeaders;
        this.body = responseBody;
    }

    public MultivaluedMap<String,Object>  getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Response getResponse() {
        return Response.ok(body).replaceAll(headers).build();
    }

    /**
     * Retrieve provided web page and build a GetResponseModel object for storage in the cache.
     *
     * @param url
     * @return GetResponseModel
     * @throws ProcessingException
     */
    public static GetResponseModel create(String url) throws ProcessingException {
        JerseyClient client = JerseyClientBuilder.createClient();
        JerseyWebTarget target = client.target(url);
        JerseyInvocation.Builder invocationBuilder = target.request();
        Response response = invocationBuilder.get();

        return new GetResponseModel(response.getHeaders(), response.readEntity(String.class));
    }
}
