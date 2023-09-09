package pl.kempa.saska.receiptproducer.service;

import java.util.List;

import pl.kempa.saska.receiptproducer.dto.GoodsReceiptDTO;

public interface ReceiptProduceService<T> {
	void produceReceipt(T dto, String exchange);

	void produceReceipt(T dto, String exchange, String routingKey);
}
