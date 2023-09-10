package pl.kempa.saska.triageservice.service.impl;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.triageservice.dto.GoodsReceiptDTO;
import pl.kempa.saska.triageservice.dto.Potential;
import pl.kempa.saska.triageservice.entity.FailedGoodsReceipt;
import pl.kempa.saska.triageservice.service.ReturnMessageService;

@Service
@Slf4j
@AllArgsConstructor
public class ReturnReceiptService implements ReturnMessageService<FailedGoodsReceipt> {

	private RabbitTemplate rabbitTemplate;
	private Jackson2JsonMessageConverter messageConverter;

	@Override
	public void returnMessage(FailedGoodsReceipt failedGoodsReceipt, String exchange) {
		var receiptDTO = new GoodsReceiptDTO(failedGoodsReceipt.getReceiptId(),
				failedGoodsReceipt.getGoodsName(), Potential.RECOVERED);
		log.info("[TRIAGE RETURN] is sending receipt {} to a queue {}", receiptDTO,
				failedGoodsReceipt.getQueueName());
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("queue", failedGoodsReceipt.getQueueName());

		Message message = messageConverter.toMessage(receiptDTO, messageProperties);
		rabbitTemplate.convertAndSend("x.goods-receipt-return", "", message);
	}
}
