package com.dmlogs.examples.resources;

import com.dmlogs.examples.models.GetResponseModel;
import com.dmlogs.examples.services.CacheService;
import org.apache.commons.validator.routines.UrlValidator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Root resource (exposed at "cache" path)
 *
 */
@Path("cache")
public class CacheResource {

    private CacheService<GetResponseModel> cacheService = CacheService.getInstance();

    private String[] urlSchemes = {"http", "https"};
    private UrlValidator validator = new UrlValidator(urlSchemes);


    private final static String cacheStatsTemplate = "<!DOCTYPE html><html>" +
            "<head><title>simple-web-page-cache</title></head>" +
            "<body><h1>simple-web-page-cache cache statistics</h1>" +
            "<p><em>Hit Count:</em>%s</p>" +
            "<p><em>Request Count:</em>%s</p>" +
            "<p><em>Hit Rate:</em>%s</p>" +
            "</body></html>";

    private final static String nullUrlMessage = "A url must be provided as a query parameter.";
    private final static String invalidUrlMessageTemplate = "The provided url was determined to not be valid: %s";
    private final static String invalidUniqueIdentifierMessageTemplate = "Id \"%s\" was not a valid unique identifier.";
    private final static String notFoundInCacheMessageTemplate = "\"%s\" was not found in the cache.";
    private final static String unknownHostMessageTemplate = "The url \"%s\" was validated successfully but the server could not reach the host.";

    /**
     *
     * @return Listing of simple cache statistics
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String cacheStats() {
        return String.format(cacheStatsTemplate, cacheService.getHitCount(), cacheService.getRequestCount(), cacheService.getHitRate());
    }

    /**
     *
     * @return Unique identifier as string returned as tet/plain response.
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response cacheUrl(@QueryParam("url") String url) {
        if (url == null) {
            return Response.status(400).entity(nullUrlMessage).build();
        }

        if (!validator.isValid(url)) {
            return Response.status(400).entity(String.format(invalidUrlMessageTemplate,url)).build();
        }

        try {
            GetResponseModel response = GetResponseModel.create(url);
            cacheService.add(response);

            return Response.ok().entity(response.getId().toString()).type(MediaType.TEXT_PLAIN).build();
        } catch(ProcessingException pe) {
            return Response.ok()
                    .entity(String.format(unknownHostMessageTemplate,url))
                    .type(MediaType.TEXT_PLAIN).build();
        }
    }

    /**
     * Expects unique identifier.
     * Retrieves content associated with the unique identifier from the cache, if exists.
     * /cache/{id}
     *
     * Response code 200 on success.
     * Response code 400 for invalid id (not valid UUID).
     * Response code 404 for missing item (expired/removed).
     *
     * @param id Unique identifier as path parameter
     * @return Content associated with unique identifier as text/html response.
     */
    @GET
    @Path("{id}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public Response retrieveFromCache(@PathParam("id") String id) {

        try {
            UUID uuid = UUID.fromString(id);
            GetResponseModel getResponseModel = cacheService.get(uuid);
            if(getResponseModel != null) {
                return getResponseModel.getResponse();
            }
        }
        catch (IllegalArgumentException iae) {
            return Response.status(400).entity(String.format(invalidUniqueIdentifierMessageTemplate,id)).type("text/plain").build();
        }

        return Response.status(404).entity(String.format(notFoundInCacheMessageTemplate,id)).build();
    }

}
