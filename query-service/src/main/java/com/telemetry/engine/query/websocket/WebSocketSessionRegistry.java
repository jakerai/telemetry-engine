package com.telemetry.engine.query.websocket;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class WebSocketSessionRegistry {

  private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

  public void register(Long userId, WebSocketSession session) {
    sessions.put(userId, session);
  }

  public void unregister(Long userId) {
    sessions.remove(userId);
  }

  public WebSocketSession getSession(Long userId) {
    return sessions.get(userId);
  }

  public Collection<WebSocketSession> getAllSessions() {
    return sessions.values();
  }

}
