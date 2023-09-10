package pl.kempa.saska.receiptproducer.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.kempa.saska.receiptproducer.dto.GoodsReceiptDTO;
import pl.kempa.saska.receiptproducer.service.ReceiptProduceService;

@RestController
@RequestMapping(value = "/api/receipts")
public class GoodsReceiptController {

	@Value("${spring.rabbitmq.exchange.receipt-produce}")
	private String receiptProduceEx;

	@Value("${spring.rabbitmq.routing-key.receipt-produce-1}")
	private String receiptProduceRK1;

	@Value("${spring.rabbitmq.routing-key.receipt-produce-2}")
	private String receiptProduceRK2;
	@Autowired private ReceiptProduceService produceService;

	@PostMapping
	public ResponseEntity<Void> publish(@RequestBody GoodsReceiptDTO receiptDTO) {
		produceService.produceReceipt(receiptDTO, receiptProduceEx,
				List.of(receiptProduceRK1, receiptProduceRK2));
		return ResponseEntity.ok().build();
	}
}
