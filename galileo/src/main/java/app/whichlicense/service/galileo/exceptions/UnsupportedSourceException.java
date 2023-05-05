/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.exceptions;

import static com.whichlicense.metadata.sourcing.MetadataSourceResolverProvider.loadChain;

public class UnsupportedSourceException extends RuntimeException {
    public UnsupportedSourceException(String source) {
        super("%s is currently unsupported for configuration %s"
                .formatted(source, loadChain()));
    }
}
