package com.telemetry.engine.auth.email;

import java.util.List;
import java.util.Map;

public interface EmailService {

  /**
   * Send email with full options. All except 'to' can be null.
   *
   * @param to List of recipient emails (required)
   * @param cc List of CC emails (optional, can be null)
   * @param bcc List of BCC emails (optional, can be null)
   * @param subject Email subject (optional, can be null)
   * @param body Email body (optional, can be null)
   * @param isHtml True if body is HTML (optional, defaults to false)
   * @param attachments Map of filename -> byte[] content (optional, can be null)
   * @param headers Custom headers (optional, can be null)
   */
  void sendEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String body,
      Boolean isHtml, Map<String, byte[]> attachments, Map<String, String> headers);

  default void sendEmail(List<String> to, String subject, String body) {
    sendEmail(to, null, null, subject, body, false, null, null);
  }

  default void sendEmail(String to, String subject, String body) {
    sendEmail(List.of(to), null, null, subject, body, false, null, null);
  }
  
   
}
