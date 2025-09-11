package com.mak.springbootefficientsearchapi.entity;


import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
public class Car {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(nullable = false)
    private String manufacturer;


    @Column(nullable = false)
    private String model;


    @Column(nullable = false)
    private String type;


    @Column(nullable = false)
    private String country;


    @Column(nullable = false)
    private LocalDate createDate;

    public Car(Integer id, String manufacturer, String model, String type, String country, LocalDate createDate) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.type = type;
        this.country = country;
        this.createDate = createDate;
    }

    public Car() {
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public Integer getId() {
        return id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public String getCountry() {
        return country;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }
}
