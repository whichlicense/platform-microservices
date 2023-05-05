/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.MalformedURLException;

import static app.whichlicense.service.galileo.exceptions.RecognizedProblems.MALFORMED_URL;

@Provider
public class MalformedUrlExceptionMapper implements ExceptionMapper<MalformedURLException> {
    @Override
    public Response toResponse(MalformedURLException exception) {
        return MALFORMED_URL.toResponse(exception);
    }
}
