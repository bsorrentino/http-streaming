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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class AsyncGeneratorBasedStreamingServlet extends HttpServlet {
    final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * task emitter
     *
     * @param emitter
     */
    private void taskEmitter( BlockingQueue<AsyncGenerator.Data<ChunkOfData>> emitter ) {

        try {

            for (int chunk = 0; chunk < 10; ++chunk) {

                TimeUnit.SECONDS.sleep(1);

                emitter.add( AsyncGenerator.Data.of(new ChunkOfData(chunk)));
            }

        }
        catch (InterruptedException e) {
            StreamingServer.log.error("got an interrupt on processing!", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Start asynchronous processing
        var asyncContext = request.startAsync();

        // Acquire a writer from response
        final PrintWriter writer = response.getWriter();

        var startAsyncTasks = AsyncGeneratorQueue.of(new LinkedBlockingQueue<>(), this::taskEmitter );

        startAsyncTasks.forEachAsync( chunk -> {
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