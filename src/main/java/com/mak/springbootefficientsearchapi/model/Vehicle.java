package com.mak.springbootefficientsearchapi.model;

import java.time.LocalDate;

public record Vehicle(String manufacturer, String model, String type, String country, LocalDate createDate) {
}
