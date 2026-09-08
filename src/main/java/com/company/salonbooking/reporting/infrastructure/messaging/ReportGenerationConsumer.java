package com.company.salonbooking.reporting.infrastructure.messaging;

import com.company.salonbooking.infrastructure.messaging.EventDeduplicationService;
import com.company.salonbooking.infrastructure.messaging.EventEnvelopeReader;
import com.company.salonbooking.infrastructure.messaging.RetryingMessageProcessor;
import com.company.salonbooking.reporting.application.usecase.ProcessReportUseCase;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.*;

@Component
public class ReportGenerationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationConsumer.class);
    private static final String CONSUMER_NAME = "report-generation-consumer";

    private final EventEnvelopeReader envelopeReader;
    private final EventDeduplicationService deduplicationService;
    private final RetryingMessageProcessor retryingMessageProcessor;
    private final ProcessReportUseCase processReportUseCase;

    public ReportGenerationConsumer(EventEnvelopeReader envelopeReader, EventDeduplicationService deduplicationService,
                                    RetryingMessageProcessor retryingMessageProcessor, ProcessReportUseCase processReportUseCase) {
        this.envelopeReader = envelopeReader;
        this.deduplicationService = deduplicationService;
        this.retryingMessageProcessor = retryingMessageProcessor;
        this.processReportUseCase = processReportUseCase;
    }

    @RabbitListener(queues = REPORT_GENERATION_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            EventEnvelopeReader.EnvelopeHeader envelope = envelopeReader.read(body);

            if (deduplicationService.alreadyProcessed(envelope.eventId(), CONSUMER_NAME)) {
                log.info("Skipping already-processed report event id={}", envelope.eventId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            UUID reportJobId = UUID.fromString(envelope.payload().get("reportJobId").asText());
            processReportUseCase.execute(reportJobId);

            if (!deduplicationService.markProcessed(envelope.eventId(), CONSUMER_NAME)) {
                log.info("Concurrent duplicate detected for report event id={}, discarding this delivery", envelope.eventId());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicAck(deliveryTag, false);
            retryingMessageProcessor.handleFailure(message, REPORT_GENERATION_RETRY_EXCHANGE,
                    REPORT_GENERATION_RETRY_QUEUE, REPORT_GENERATION_DLQ, CONSUMER_NAME, e);
        }
    }
}