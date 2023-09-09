package pl.kempa.saska.triageservice.dto;

public record FailedGoodsReceiptDTO(String id, String goodsName, String queueName, String causeMessage) {
}
