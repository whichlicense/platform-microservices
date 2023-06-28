/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

module whichlicense.service.nebula {
    requires jakarta.ws.rs;
    requires jakarta.cdi;
    requires whichlicense.identity;
    requires whichlicense.identification.license;
    requires com.fasterxml.jackson.databind;
}
