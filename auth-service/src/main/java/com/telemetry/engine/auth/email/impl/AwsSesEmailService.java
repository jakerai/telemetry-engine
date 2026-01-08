package com.telemetry.engine.auth.email.impl;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.aws.props.AwsProperties;
import com.telemetry.engine.auth.email.EmailService;
import jakarta.activation.DataHandler;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

@RequiredArgsConstructor
@Service
public class AwsSesEmailService implements EmailService {
  private static final Logger log = LoggerFactory.getLogger(AwsSesEmailService.class);

  private final SesClient sesClient;

  private final AwsProperties props;

  @Override
  public void sendEmail(List<String> to, List<String> cc, List<String> bcc, String subject,
      String body, Boolean isHtml, Map<String, byte[]> attachments, Map<String, String> headers) {
    log.info(
        "[AwsSesEmailService.sendEmail] Sending email: to={} cc={} bcc={} subject='{}' isHtml={} attachments={} headers={}",
        to, cc, bcc, subject, isHtml, attachments != null ? attachments.keySet() : null, headers);

    try {
      if (attachments != null && !attachments.isEmpty()) {
        sendEmailWithAttachments(to, cc, bcc, subject, body, isHtml, attachments);
      } else {
        sendSimpleEmail(to, cc, bcc, subject, body, isHtml);
      }
    } catch (Exception e) {
      log.warn("Error while sending email via AWS SES", e);
      throw new RuntimeException("Failed to send email via AWS SES", e);
    }
  }

  private void sendSimpleEmail(List<String> to, List<String> cc, List<String> bcc, String subject,
      String body, Boolean isHtml) {
    log.info(
        "[AwsSesEmailService.sendSimpleEmail] Sending email: to={} cc={} bcc={} subject='{}' isHtml={}",
        to, cc, bcc, subject, isHtml);

    Destination destination =
        Destination.builder().toAddresses(to).ccAddresses(cc != null ? cc : List.of())
            .bccAddresses(bcc != null ? bcc : List.of()).build();

    /* HTML based email content only support inline style css */
    Body messageBody =
        Body.builder().html(isHtml != null && isHtml ? Content.builder().data(body).build() : null)
            .text(isHtml != null && isHtml ? null : Content.builder().data(body).build()).build();

    Message message = Message.builder().subject(Content.builder().data(subject).build())
        .body(messageBody).build();

    SendEmailRequest request = SendEmailRequest.builder().destination(destination).message(message)
        .source(props.getSesVerifiedSenderEmail()).build();

    sesClient.sendEmail(request);
  }

  private void sendEmailWithAttachments(List<String> to, List<String> cc, List<String> bcc,
      String subject, String body, Boolean isHtml, Map<String, byte[]> attachments)
      throws MessagingException {
    log.info(
        "[AwsSesEmailService.sendEmailWithAttachments] Sending email with attachments: to={} cc={} bcc={} subject='{}' isHtml={} attachmentNames={}",
        to, cc, bcc, subject, isHtml, attachments != null ? attachments.keySet() : null);


    Session session = Session.getDefaultInstance(new Properties());
    MimeMessage message = new MimeMessage(session);
    message.setSubject(subject);
    message.setFrom(new InternetAddress(props.getSesVerifiedSenderEmail()));

    // Setting recipients
    for (String recipient : to) {
      message.addRecipient(RecipientType.TO, new InternetAddress(recipient));
    }
    if (cc != null) {
      for (String recipient : cc) {
        message.addRecipient(RecipientType.CC, new InternetAddress(recipient));
      }
    }
    if (bcc != null) {
      for (String recipient : bcc) {
        message.addRecipient(RecipientType.BCC, new InternetAddress(recipient));
      }
    }

    MimeMultipart multipart = new MimeMultipart();
    MimeBodyPart bodyPart = new MimeBodyPart();
    if (isHtml != null && isHtml) {
      bodyPart.setContent(body, "text/html; charset=utf-8");
    } else {
      bodyPart.setText(body);
    }
    multipart.addBodyPart(bodyPart);


    // Attachments
    for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
      MimeBodyPart attachmentPart = new MimeBodyPart();
      attachmentPart.setFileName(entry.getKey());
      /**
       * Creating a DataSource from the byte array application/octet-stream is a safe default for
       * binary data
       */
      ByteArrayDataSource dataSource =
          new ByteArrayDataSource(entry.getValue(), "application/octet-stream");
      attachmentPart.setDataHandler(new DataHandler(dataSource));
      multipart.addBodyPart(attachmentPart);
    }

    message.setContent(multipart);

    // Converting MimeMessage to RawMessage for SES
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      message.writeTo(outputStream);
      RawMessage rawMessage =
          RawMessage.builder().data(SdkBytes.fromByteArray(outputStream.toByteArray())).build();
      SendRawEmailRequest rawEmailRequest =
          SendRawEmailRequest.builder().rawMessage(rawMessage).build();
      sesClient.sendRawEmail(rawEmailRequest);
    } catch (Exception e) {
      log.warn("Error while sending email with attachments via AWS SES", e);
      throw new RuntimeException("Failed to send raw email via AWS SES", e);
    }
  }

}
