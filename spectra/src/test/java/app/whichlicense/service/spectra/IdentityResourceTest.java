/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.spectra;

import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@HelidonTest
class IdentityResourceTest {
    @Inject
    private MetricRegistry registry;
    @Inject
    private WebTarget target;

    @Test
    void testMicroprofileMetrics() {
        var identity = target.path("identity").request().get(String.class);
        assertThat(identity).isNotBlank();

        var counter = registry.counter("identityGets");
        var before = counter.getCount();

        identity = target.path("identity").request().get(String.class);
        assertThat(identity).isNotBlank();

        var after = counter.getCount();
        assertThat(after - before).isEqualTo(1d)
                .withFailMessage("Difference in identity counter between successive calls");
    }

    @Test
    void testMetrics() {
        var response = target.path("metrics").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testHealth() {
        var response = target.path("health").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testIdentityGeneration() {
        var identity = target.path("identity").request().get(String.class);
        assertThat(identity).isNotBlank();
    }
}
