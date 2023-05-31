package com.mak.springbootefficientsearchapi.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    private String manufacturer;
    private String model;
    private String type;
    private String country;
    private LocalDate createDate;
}
