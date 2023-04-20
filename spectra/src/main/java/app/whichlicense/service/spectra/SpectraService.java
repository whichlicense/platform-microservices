/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.spectra;

import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

import static com.whichlicense.metadata.identity.Identity.toHex;
import static com.whichlicense.metadata.identity.Identity.wrapAndGenerate;
import static io.helidon.common.http.Http.HeaderValues.CONTENT_TYPE_TEXT_PLAIN;
import static java.lang.Integer.parseUnsignedInt;
import static java.lang.Thread.currentThread;

/**
 * SpectraService is a class representing a web server that generates a plain text response
 * containing a unique metadata identity spectra.
 *
 * @author David Greven
 * @version 0
 * @since 0.0.0
 */
public class SpectraService {
    /**
     * The main method of SpectraService class that starts the web server.
     *
     * @param args an array of command-line arguments used to specify the host and port to
     *             listen to
     * @since 0.0.0
     */
    public static void main(String[] args) {
        WebServer.builder()
                .routing(SpectraService::routing)
                .host(args[0])
                .port(parseUnsignedInt(args[1]))
                .start();
    }

    /**
     * The routing method of SpectraService class that adds routing rules to the web server.
     *
     * @param rules a HttpRouting.Builder object that defines routing rules for the web server
     * @since 0.0.0
     */
    private static void routing(HttpRouting.Builder rules) {
        rules.get(SpectraService::call);
    }

    /**
     * The call method of SpectraService class that generates a hexadecimal metadata identity
     * spectra representation based on the current thread's ID and sends it as a plain text
     * response to the client.
     *
     * @param ignored a ServerRequest object that represents the incoming HTTP request (ignored)
     * @param res     a ServerResponse object that represents the outgoing HTTP response
     * @since 0.0.0
     */
    private static void call(ServerRequest ignored, ServerResponse res) {
        res.header(CONTENT_TYPE_TEXT_PLAIN).send(toHex(wrapAndGenerate(currentThread().threadId())));
    }
}
