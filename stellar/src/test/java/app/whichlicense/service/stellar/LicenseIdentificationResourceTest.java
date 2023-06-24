/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.stellar;

import com.whichlicense.metadata.identification.license.LicenseMatch;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

@HelidonTest
class LicenseIdentificationResourceTest {
    @Inject
    private MetricRegistry registry;
    @Inject
    private WebTarget target;

    @Test
    void testMicroprofileMetrics() {
        var identity = target.path("identify").request()
                .post(Entity.entity("", TEXT_PLAIN_TYPE), LicenseMatch.class);
        assertThat(identity).isNotNull();

        var counter = registry.counter("identifyGets");
        var before = counter.getCount();

        identity = target.path("identify").request()
                .post(Entity.entity("", TEXT_PLAIN_TYPE), LicenseMatch.class);
        assertThat(identity).isNotNull();

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
    void testLicenseIdentification() {
        var identity = target.path("identify").request()
                .post(Entity.entity("""
                        Copyright <YEAR> <COPYRIGHT HOLDER>
                                                
                        Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
                                                
                        The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
                                                
                        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
                        """, TEXT_PLAIN_TYPE), LicenseMatch.class);
        assertThat(identity).isNotNull();
    }
}
