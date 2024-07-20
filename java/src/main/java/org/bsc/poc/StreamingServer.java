package org.bsc.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * LangGraphStreamingServer is an interface that represents a server that supports streaming
 * of LangGraph.
 * Implementations of this interface can be used to create a web server
 * that exposes an API for interacting with compiled language graphs.
     */
public interface StreamingServer {

    Logger log = LoggerFactory.getLogger(StreamingServer.class);

    CompletableFuture<Void> start() throws Exception;

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private int port = 8080;

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public StreamingServer build() throws Exception {

            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(port);
            server.addConnector(connector);

            ResourceHandler resourceHandler = new ResourceHandler();

            Resource baseResource = ResourceFactory.of(resourceHandler).newClassLoaderResource("webapp");
            resourceHandler.setBaseResource(baseResource);

            resourceHandler.setDirAllowed(true);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            // Add the streaming servlet
            context.addServlet(new ServletHolder(new ThreadBasedStreamingServlet()), "/stream");

            Handler.Sequence handlerList = new Handler.Sequence(resourceHandler, context);

            server.setHandler(handlerList);

            return () -> CompletableFuture.runAsync(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, Runnable::run);

        }
    }
}

record ChunkOfData( int index, String value ) {
    public ChunkOfData( int index ) {
        this( index, "Item"+ index );
    }
}
class ThreadBasedStreamingServlet extends HttpServlet {
    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Start asynchronous processing
        var asyncContext = request.startAsync();

        // Acquire a writer from response
        final PrintWriter writer = response.getWriter();

        CompletableFuture.runAsync(() -> {
            try {

                for (int chunk = 0; chunk < 10; ++chunk) {

                    var data = new ChunkOfData(chunk);

                    try {
                        var serializedData = objectMapper.writeValueAsString(data);
                        writer.println(serializedData);
                    } catch (IOException e) {
                        StreamingServer.log.warn("error serializing data!. skip it.", e);
                    }
                    writer.flush();
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                StreamingServer.log.error("got an interrupt on processing!", e);
                throw new RuntimeException(e);
            }
        }).whenComplete((result, ex) -> {
            writer.close();
            asyncContext.complete();
        });
    }
}


