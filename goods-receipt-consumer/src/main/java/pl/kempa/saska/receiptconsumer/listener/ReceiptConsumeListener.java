package pl.kempa.saska.receiptconsumer.listener;

import java.util.Random;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.receiptconsumer.dto.GoodsReceiptDTO;
import pl.kempa.saska.receiptconsumer.dto.Potential;

@Component
@Slf4j
public class ReceiptConsumeListener {

	private int queueNum;

	public ReceiptConsumeListener() {
		var random = new Random();
		this.queueNum = random.nextInt(2) + 1;
	}

	@RabbitListener(queues = {
			"${spring.rabbitmq.queue.receipt-produce-1}",
			"${spring.rabbitmq.queue.receipt-produce-2}"
	})
	public void onReceiptConsume(GoodsReceiptDTO receiptDTO, @Header("amqp_consumerQueue") String queue) {
		if (receiptDTO.potential().equals(Potential.ERROR) && queue.contains(String.valueOf(this.queueNum))) {
			log.warn("[CONSUMER EXCEPTION] there is an exception for goods receipt {} from [QUEUE] {}", receiptDTO,
					queue);
			throw new UnsupportedOperationException("Some randon exception");
		}
		log.info("[CONSUMER] is receiving goods receipt {} from [QUEUE] {}", receiptDTO, queue);
	}
}
