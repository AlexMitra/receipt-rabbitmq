package pl.kempa.saska.triageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.kempa.saska.triageservice.entity.DeadGoodsReceipt;

@Repository
public interface DeadGoodsReceiptRepository extends JpaRepository<DeadGoodsReceipt, Long> {
}
