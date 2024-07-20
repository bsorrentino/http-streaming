package org.bsc.poc;

public class StreamingServerTest {


    public static void main(String[] args) throws Exception {

        var server = StreamingServer.builder()
                                                .port(8080)
                                                .build();

        server.start().join();

    }

}
