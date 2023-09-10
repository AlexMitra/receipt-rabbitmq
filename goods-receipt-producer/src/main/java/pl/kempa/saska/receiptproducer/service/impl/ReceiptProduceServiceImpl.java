package pl.kempa.saska.receiptproducer.service.impl;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.receiptproducer.dto.GoodsReceiptDTO;
import pl.kempa.saska.receiptproducer.service.ReceiptProduceService;

@Service
@AllArgsConstructor
@Slf4j
public class ReceiptProduceServiceImpl implements ReceiptProduceService<GoodsReceiptDTO> {

	private RabbitTemplate rabbitTemplate;

	@Override
	public void produceReceipt(GoodsReceiptDTO dto, String exchange) {
		log.info("[PRODUCER] is sending goods receipt {}", dto);
		rabbitTemplate.convertAndSend(exchange, "", dto);
	}

	@Override
	public void produceReceipt(GoodsReceiptDTO dto, String exchange, List<String> routingKeys) {
		log.info("[PRODUCER] is sending goods receipt {}", dto);
		routingKeys.parallelStream().forEach(rk -> rabbitTemplate.convertAndSend(exchange, rk, dto));
	}
}
