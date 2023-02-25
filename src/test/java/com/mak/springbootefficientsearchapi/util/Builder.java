package com.mak.springbootefficientsearchapi.util;

import com.mak.springbootefficientsearchapi.entity.Car;
import com.mak.springbootefficientsearchapi.model.Vehicle;

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
    public static Vehicle vehicle(String manufacturer, String model, String type, String country, LocalDate createDate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setManufacturer(manufacturer);
        vehicle.setModel(model);
        vehicle.setType(type);
        vehicle.setCountry(country);
        vehicle.setCreateDate(createDate);
        return vehicle;
    }

}
