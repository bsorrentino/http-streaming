package org.bsc.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

                    TimeUnit.SECONDS.sleep(1);
                    var data = new ChunkOfData(chunk);

                    try {
                        var serializedData = objectMapper.writeValueAsString(data);
                        writer.println(serializedData);
                    } catch (IOException e) {
                        StreamingServer.log.warn("error serializing data!. skip it.", e);
                    }
                    writer.flush();
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
