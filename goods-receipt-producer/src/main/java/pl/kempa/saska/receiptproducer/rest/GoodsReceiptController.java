package pl.kempa.saska.receiptproducer.rest;

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
	@Autowired private ReceiptProduceService produceService;

	@PostMapping
	public ResponseEntity<Void> publish(@RequestBody GoodsReceiptDTO receiptDTO) {
		produceService.produceReceipt(receiptDTO, receiptProduceEx);
		return ResponseEntity.ok().build();
	}
}
