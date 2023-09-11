package pl.kempa.saska.triageservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.triageservice.dto.GoodsReceiptDTO;
import pl.kempa.saska.triageservice.entity.DeadGoodsReceipt;
import pl.kempa.saska.triageservice.repository.DeadGoodsReceiptRepository;

@Component
@Slf4j
@AllArgsConstructor
public class DeadReceiptListener {

	private DeadGoodsReceiptRepository receiptRepository;

	@RabbitListener(queues = {"${spring.rabbitmq.queue.dead-letter}"})
	public void onDLQConsume(GoodsReceiptDTO receiptDTO) {
		log.info("[DLQ] is receiving poison receipt {} from queue {}", receiptDTO, "test");
		var entity = new DeadGoodsReceipt();
		entity.setReceiptId(receiptDTO.id());
		entity.setGoodsName(receiptDTO.goodsName());
		entity.setQueueName("test");
		entity.setCurrentTimeMs(System.currentTimeMillis());
		log.info("[DLQ] is saving poison receipt {} to DB", entity);
		receiptRepository.save(entity);
	}
}
