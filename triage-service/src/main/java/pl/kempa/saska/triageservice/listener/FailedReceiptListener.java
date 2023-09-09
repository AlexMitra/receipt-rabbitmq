package pl.kempa.saska.triageservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.triageservice.dto.FailedGoodsReceiptDTO;
import pl.kempa.saska.triageservice.entity.FailedGoodsReceipt;
import pl.kempa.saska.triageservice.repository.FailedGoodsReceiptRepository;

@Component
@Slf4j
@AllArgsConstructor
public class FailedReceiptListener {

	private FailedGoodsReceiptRepository receiptRepository;

	@RabbitListener(queues = {"${spring.rabbitmq.queue.receipt-failed}"})
	public void onFailedReceiptConsume(FailedGoodsReceiptDTO failedReceiptDTO) {
		log.info("[TRIAGE] is receiving failed goods receipt {} from queue {}", failedReceiptDTO,
				failedReceiptDTO.queueName());
		var entity = new FailedGoodsReceipt();
		entity.setReceiptId(failedReceiptDTO.id());
		entity.setGoodsName(failedReceiptDTO.goodsName());
		entity.setQueueName(failedReceiptDTO.queueName());
		entity.setCauseMessage(failedReceiptDTO.causeMessage());
		entity.setCurrentTimeMs(System.currentTimeMillis());
		log.info("[TRIAGE] is saving failed goods receipt {} to DB", failedReceiptDTO);
		receiptRepository.save(entity);
	}
}
