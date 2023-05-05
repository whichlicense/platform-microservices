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

import static app.whichlicense.service.galileo.exceptions.RecognizedProblems.UNSUPPORTED_SOURCE;

@Provider
public class UnsupportedSourceExceptionMapper implements ExceptionMapper<UnsupportedSourceException> {
    @Override
    public Response toResponse(UnsupportedSourceException exception) {
        return UNSUPPORTED_SOURCE.toResponse(exception);
    }
}
