package com.mak.springbootefficientsearchapi.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

}
