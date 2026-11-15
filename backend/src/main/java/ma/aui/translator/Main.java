package ma.aui.translator;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

/** Starts the embedded Grizzly server with the Jersey (Jakarta REST) application. */
public class Main {
    public static void main(String[] args) throws Exception {
        ResourceConfig app = new ResourceConfig()
                .register(TranslatorResource.class)
                .register(BasicAuthFilter.class)
                .register(CorsFilter.class)
                .register(JacksonFeature.class);

        URI uri = URI.create("http://" + Config.get("server.host") + ":" + Config.getInt("server.port") + "/api/");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, app);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        System.out.println("Darija Translator running at " + uri + "translate");
        System.out.println("Press Ctrl+C to stop.");
        Thread.currentThread().join();
    }
}
