package pl.kempa.saska.triageservice.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "failed_good_receipts")
public class FailedGoodsReceipt implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String receiptId;
	private String goodsName;
	private String queueName;
	private String causeMessage;
	private Long currentTimeMs;
}
