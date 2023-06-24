/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

module whichlicense.service.stellar {
    requires java.logging;
    requires jakarta.ws.rs;
    requires jakarta.cdi;
    requires io.helidon.metrics;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires whichlicense.problem;
    requires whichlicense.identification.license;
}
