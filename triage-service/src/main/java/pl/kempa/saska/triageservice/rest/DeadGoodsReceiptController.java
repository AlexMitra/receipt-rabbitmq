package pl.kempa.saska.triageservice.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kempa.saska.triageservice.entity.DeadGoodsReceipt;
import pl.kempa.saska.triageservice.repository.DeadGoodsReceiptRepository;

@RestController
@RequestMapping(value = "/api/dead-receipts")
@AllArgsConstructor
@Slf4j
public class DeadGoodsReceiptController {

	private DeadGoodsReceiptRepository repository;

	@GetMapping
	public ResponseEntity<List<DeadGoodsReceipt>> getAll() {
		return ResponseEntity.ok(repository.findAll());
	}
}
