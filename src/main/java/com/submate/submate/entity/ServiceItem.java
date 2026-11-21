package com.submate.submate.entity;

import jakarta.persistence.*;

// 클래스 이름을 Service 로 하면 스프링의 @Service 와 이름이 겹쳐서 ServiceItem 으로 함
@Entity
@Table(name = "services")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    protected ServiceItem() {
    }

    public Integer getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
