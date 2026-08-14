package com.dawsons.laundry.repository;

import com.dawsons.laundry.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillItemRepository extends JpaRepository<BillItem, Integer> {
}
