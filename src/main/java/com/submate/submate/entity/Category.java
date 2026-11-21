package com.submate.submate.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    protected Category() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
