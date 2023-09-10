package pl.kempa.saska.receiptconsumer.config;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.receiptconsumer.dto.FailedGoodsReceiptDTO;
import pl.kempa.saska.receiptconsumer.dto.GoodsReceiptDTO;

@Slf4j
public class ReceiptErrorMessageResolver implements MessageRecoverer {
	private RabbitTemplate template;
	private Jackson2JsonMessageConverter converter;

	@Value("${spring.rabbitmq.exchange.receipt-failed}")
	private String receiptFailedEx;

	public ReceiptErrorMessageResolver(RabbitTemplate template, Jackson2JsonMessageConverter converter) {
		this.template = template;
		this.converter = converter;
	}

	@Override
	public void recover(Message message, Throwable cause) {
		if (cause instanceof ListenerExecutionFailedException) {
			try {
				message.getMessageProperties().setInferredArgumentType(GoodsReceiptDTO.class);
				var receiptDTO = (GoodsReceiptDTO) converter.fromMessage(message, GoodsReceiptDTO.class);
				var sourceQueue = message.getMessageProperties().getConsumerQueue();
				var failedMessage = new FailedGoodsReceiptDTO(receiptDTO.id(), receiptDTO.goodsName(), sourceQueue,
						cause.getCause().getMessage());
				log.info("[RECOVERER] backOff limit has finished for receipt {}", receiptDTO);
				log.info("[RECOVERER] is moving failed receipt {} for triage", receiptDTO);
				this.template.convertAndSend(receiptFailedEx, "", failedMessage);
			} catch (Exception ex) {
				//send the message to dead letter queue
				throw new AmqpRejectAndDontRequeueException("Unable to recover message", ex);
			}
		} else {
			//send the message to dead letter queue
			throw new AmqpRejectAndDontRequeueException("Unable to recover message");
		}
	}
}
