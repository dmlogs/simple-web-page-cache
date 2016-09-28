package com.dmlogs.examples;

import com.dmlogs.examples.services.ConfigurationService;
import org.glassfish.jersey.internal.util.Base64;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class AuthenticationRequestFilter implements ContainerRequestFilter {
    private ConfigurationService config = ConfigurationService.getInstance();

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        if (containerRequestContext.getUriInfo().getPath().equals("application.wadl")) {
            return; // No authorization required.
        }

        String authorization = containerRequestContext.getHeaderString("Authorization");

        if (authorization != null) {
            String[] authorizationComponents = authorization.split(" ");

            if (authorizationComponents.length == 2 && authorizationComponents[0].equals("Basic")) {
                String[] authenticationComponents = Base64.decodeAsString(authorizationComponents[1]).split(":");
                if (authenticationComponents.length == 2 &&
                        authenticationComponents[0].equals(config.getUser()) &&
                        authenticationComponents[1].equals(config.getPassword())) {
                    return; // Authorized.
                }
            }
        }

        containerRequestContext.abortWith(Response.status(403).type("text/plain").entity("Forbidden.").build());
    }
}
