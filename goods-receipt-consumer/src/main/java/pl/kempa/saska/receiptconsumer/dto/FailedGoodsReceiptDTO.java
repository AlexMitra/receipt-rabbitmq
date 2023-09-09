package pl.kempa.saska.receiptconsumer.dto;

public record FailedGoodsReceiptDTO(String id, String goodsName, String queueName, String causeMessage) {
}
