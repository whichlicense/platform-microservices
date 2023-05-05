/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.exceptions;

import com.whichlicense.problem.Problem;
import com.whichlicense.problem.StaticProblemMembers;
import jakarta.ws.rs.core.Response;

import java.net.URI;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.fromStatusCode;

public enum RecognizedProblems implements StaticProblemMembers {
    MALFORMED_URL(BAD_REQUEST, "Invalid discovery url", "https://api.whichlicense.app/problem/discovery/invalid-url"),
    UNSUPPORTED_SOURCE(BAD_REQUEST, "Unsupported discovery source", "https://api.whichlicense.app/problem/discovery/invalid-url");

    private final int status;
    private final String title;
    private final URI type;

    RecognizedProblems(int status, String title, URI type) {
        this.status = status;
        this.title = title;
        this.type = type;
    }

    RecognizedProblems(Response.Status status, String title, String type) {
        this(status.getStatusCode(), title, URI.create(type));
    }

    @Override
    public URI type() {
        return type;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public int status() {
        return status;
    }

    public Response toResponse(Exception exception) {
        var placeholder = URI.create("https://api.whichlicense.app/placeholder");
        return Response.status(fromStatusCode(status)).entity(Problem.builder(this)
                .detail(exception.getMessage()).instance(placeholder).build()).build();
    }
}
