package com.submate.submate.repository;

import com.submate.submate.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {

    // 카테고리별 서비스 목록 (sort_order 오름차순)
    List<ServiceItem> findByCategoryIdOrderBySortOrderAsc(Integer categoryId);
}
