package com.mak.springbootefficientsearchapi;

import com.mak.springbootefficientsearchapi.entity.Car;
import com.mak.springbootefficientsearchapi.repository.CarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
public class SpringBootEfficientSearchApiApplication implements CommandLineRunner {

    @Autowired
    private CarRepository carRepository;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootEfficientSearchApiApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {

        File file = ResourceUtils.getFile("classpath:static/Cars.csv");
        long header = 1L;
        List<Car> carList = Files.lines(file.toPath())
                                 .parallel()
                                 .skip(header)
                                 .map(createCarForInit())
                                 .collect(Collectors.toList());
        carRepository.saveAll(carList);

    }

    private Function<String, Car> createCarForInit() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return line -> {
            String[] data = line.split(";");
            Car car = new Car();
            car.setType(data[1]);
            car.setCountry(data[2]);
            car.setManufacturer(data[3]);
            car.setCreateDate(LocalDate.parse(data[4], formatter));
            car.setModel(data[5]);
            return car;
        };
    }

}


