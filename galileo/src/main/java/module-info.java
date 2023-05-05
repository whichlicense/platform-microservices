/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
import com.whichlicense.metadata.seeker.MetadataSeeker;

module whichlicense.service.galileo {
    requires jakarta.cdi;
    requires jakarta.ws.rs;
    requires whichlicense.logging;
    requires whichlicense.problem;
    requires whichlicense.sourcing;
    requires whichlicense.sourcing.github;
    requires whichlicense.seeker.npm;
    requires whichlicense.seeker.yarn;
    requires whichlicense.seeker.license;
    requires whichlicense.seeker.notice;
    requires whichlicense.seeker.readme;
    requires whichlicense.seeker.gitignore;
    requires whichlicense.seeker.gitattributes;
    requires whichlicense.seeker.gitmodules;
    requires whichlicense.seeker.gitrepo;
    requires whichlicense.seeker.rat;
    requires whichlicense.integration.jackson.identity;
    requires whichlicense.identification.license;
    requires whichlicense.identification.license.backend.panama;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    opens app.whichlicense.service.galileo.npm to com.fasterxml.jackson.databind;
    opens app.whichlicense.service.galileo.simplesbom to com.fasterxml.jackson.databind;
    uses MetadataSeeker;
}
