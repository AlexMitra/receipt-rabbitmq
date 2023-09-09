package pl.kempa.saska.triageservice.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.triageservice.dto.FailedReceiptsTriageDTO;
import pl.kempa.saska.triageservice.entity.FailedGoodsReceipt;
import pl.kempa.saska.triageservice.repository.FailedGoodsReceiptRepository;
import pl.kempa.saska.triageservice.service.ReturnMessageService;

@RestController
@RequestMapping(value = "/api/failed-receipts")
@Slf4j
public class FailedGoodsReceiptController {

	@Autowired private FailedGoodsReceiptRepository repository;
	@Autowired private ReturnMessageService returnMessageService;
	@Value("${spring.rabbitmq.exchange.receipt-return}")
	private String receiptReturnEx;

	@GetMapping
	public ResponseEntity<List<FailedGoodsReceipt>> getAll() {
		return ResponseEntity.ok(repository.findAll());
	}

	@PostMapping
	public ResponseEntity<Void> resend(@RequestBody FailedReceiptsTriageDTO triageDTO) {
		if (triageDTO.ids().equalsIgnoreCase("ALL")) {
			var failedReceipts = repository.findAll();
			failedReceipts.forEach(receipt -> {
				returnMessageService.returnMessage(receipt, receiptReturnEx);
				repository.delete(receipt);
			});
		}
		return ResponseEntity.ok().build();
	}
}
