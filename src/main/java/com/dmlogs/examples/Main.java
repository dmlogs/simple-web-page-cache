package com.dmlogs.examples;

import com.dmlogs.examples.services.ConfigurationService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class.
 *
 * Starts a Grizzly HTTP server.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static String BASE_URI = null;
    public static ConfigurationService config = ConfigurationService.getInstance();;

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.dmlogs.examples package
        final ResourceConfig rc = new ResourceConfig().packages("com.dmlogs.examples.resources");

        if (config.isAuthenticationEnabled()) {
            rc.register(com.dmlogs.examples.AuthenticationRequestFilter.class);
        }

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        try {
            config.configure(args);
        } catch(NumberFormatException nfe) {
            Logger.getLogger(Main.class.getSimpleName()).log(Level.SEVERE, nfe.getMessage(), nfe);
            System.out.println("Failed to parse arguments to configuration. Terminating...");
            return;
        }

        BASE_URI = String.format("http://localhost:%d/",config.getPort());

        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }


}

