package com.crediya;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.HashMap;
import java.util.Map;

public class SqsMessageHandler implements RequestHandler<SQSEvent, Void> {

  private final SesClient sesClient = SesClient.builder().region(Region.US_EAST_1).build();
  private final Gson gson = new Gson();

  @Override
  public Void handleRequest(SQSEvent sqsEvent, Context context) {
    LambdaLogger logger = context.getLogger();
    logger.log("Se ha recibido un lote de " + sqsEvent.getRecords().size() + " mensajes de SQS.");
    for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
      try {
        String messageId = message.getMessageId();
        String messageBody = message.getBody();
        logger.log("Procesando Mensaje ID: " + messageId);
        logger.log("Cuerpo del Mensaje: " + messageBody);
        SqsMessageBody bodyObject = gson.fromJson(messageBody, SqsMessageBody.class);
        String subject = bodyObject.getSubject();
        String toEmail = bodyObject.getEmail();
        String messageContent = bodyObject.getMessage();

        boolean isApproved = messageContent.toLowerCase().contains("aprobada");
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("status", isApproved ? "Aprobada" : "Rechazada");
        templateData.put("isApproved", isApproved);
        templateData.put("isRejected", !isApproved);
        templateData.put("paymentPlan", bodyObject.getPaymentPlan());
        String templateDataJson = gson.toJson(templateData);
        String FROM_EMAIL = System.getenv("FROM_EMAIL");
        String TEMPLATE_NAME = "CrediYaStatusTemplate";
        Destination destination = Destination.builder().toAddresses(toEmail).build();
        SendTemplatedEmailRequest templatedEmailRequest = SendTemplatedEmailRequest
          .builder()
          .source(FROM_EMAIL)
          .destination(destination)
          .template(TEMPLATE_NAME)
          .templateData(templateDataJson)
          .build();
        logger.log("SendTemplatedEmailRequest => " + templatedEmailRequest);
        logger.log("Enviando mensaje con SES a: " + toEmail);
        sesClient.sendTemplatedEmail(templatedEmailRequest);
        logger.log("Mensaje " + messageId + " enviado exitosamente con SES.");
      } catch (SesException e) {
        logger.log("ERROR al enviar el mensaje con SES: " + e.awsErrorDetails().errorMessage());
        throw new RuntimeException("Fallo al enviar con SES.", e);
      } catch (Exception e) {
        logger.log("ERROR al procesar el mensaje: " + message.getMessageId() + ". Error: " + e.getMessage());
        throw new RuntimeException("Fallo al procesar un mensaje, se reintentar el lote completo.", e);
      }
    }
    logger.log("Lote de mensajes procesado exitosamente.");
    return null;
  }

}
