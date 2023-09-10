package pl.kempa.saska.receiptproducer.service;

import java.util.List;

public interface ReceiptProduceService<T> {
	void produceReceipt(T dto, String exchange);

	void produceReceipt(T dto, String exchange, List<String> routingKeys);
}
