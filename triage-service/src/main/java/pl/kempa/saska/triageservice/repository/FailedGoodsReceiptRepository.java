package pl.kempa.saska.triageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.kempa.saska.triageservice.entity.FailedGoodsReceipt;

@Repository
public interface FailedGoodsReceiptRepository extends JpaRepository<FailedGoodsReceipt, Long> {
}
