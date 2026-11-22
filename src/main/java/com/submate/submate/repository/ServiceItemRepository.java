package com.submate.submate.repository;

import com.submate.submate.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {

    // 카테고리별 서비스 목록 (sort_order 오름차순)
    List<ServiceItem> findByCategoryIdOrderBySortOrderAsc(Integer categoryId);

    // 서비스 목록에 있는 이름이면 service_id 연결용
    Optional<ServiceItem> findByCategoryIdAndName(Integer categoryId, String name);
}
