package com.mak.springbootefficientsearchapi.util;

import com.mak.springbootefficientsearchapi.entity.Car;

import java.time.LocalDate;

public class Builder {

    public static Car car(Integer id, String manufacturer, String model, String type, String country, LocalDate createDate) {
        Car car = new Car();
        car.setId(id);
        car.setManufacturer(manufacturer);
        car.setModel(model);
        car.setType(type);
        car.setCountry(country);
        car.setCreateDate(createDate);
        return car;
    }

}
