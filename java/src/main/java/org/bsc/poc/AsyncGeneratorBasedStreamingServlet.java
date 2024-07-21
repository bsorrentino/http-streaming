package org.bsc.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bsc.async.AsyncGenerator;
import org.bsc.async.AsyncGeneratorQueue;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AsyncGeneratorBasedStreamingServlet extends HttpServlet {
    final ObjectMapper objectMapper = new ObjectMapper();

    private AsyncGenerator<ChunkOfData> startAsyncTask() {

        return AsyncGeneratorQueue.of(new LinkedBlockingQueue<>(), emitter -> {
            try {

                for (int chunk = 0; chunk < 10; ++chunk) {

                    var data = new ChunkOfData(chunk);

                    emitter.put( AsyncGenerator.Data.of(data) );

                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                StreamingServer.log.error("got an interrupt on processing!", e);
                throw new RuntimeException(e);
            }

        });
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Start asynchronous processing
        var asyncContext = request.startAsync();

        // Acquire a writer from response
        final PrintWriter writer = response.getWriter();

        startAsyncTask().forEachAsync( chunk -> {

            try {
                var serializedData = objectMapper.writeValueAsString(chunk);
                writer.println(serializedData);
            } catch (IOException e) {
                StreamingServer.log.warn("error serializing data!. skip it.", e);
            }
            writer.flush();

        }).whenComplete((result, ex) -> {
            writer.close();
            asyncContext.complete();
        });
    }
}