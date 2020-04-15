package com.mak.springbootefficientsearchapi;

import com.mak.springbootefficientsearchapi.service.CarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import java.io.File;

@Slf4j
@SpringBootApplication
public class SpringBootEfficientSearchApiApplication implements CommandLineRunner {

    @Autowired
    private CarService carService;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootEfficientSearchApiApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        File file = ResourceUtils.getFile("classpath:static/Cars.csv");
        carService.init(file);
    }

}
