/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.meteor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import static java.util.logging.Logger.getLogger;

@ServerEndpoint("/observed")
@ApplicationScoped
public class ObservedInteractionResource {
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        getLogger("whichlicense.meteor").finest(message);
    }

    @OnClose
    public void onClose(Session session) {
        this.session = null;
    }

    public Session getSession() {
        return session;
    }
}
