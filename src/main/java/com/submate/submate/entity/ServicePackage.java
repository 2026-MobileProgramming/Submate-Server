package com.submate.submate.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "service_packages")
public class ServicePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceItem service;

    @Column(name = "package_name", nullable = false, unique = true, length = 150)
    private String packageName;

    protected ServicePackage() {
    }

    public Integer getId() {
        return id;
    }

    public ServiceItem getService() {
        return service;
    }

    public String getPackageName() {
        return packageName;
    }
}
