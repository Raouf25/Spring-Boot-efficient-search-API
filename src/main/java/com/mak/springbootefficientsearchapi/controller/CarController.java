package com.mak.springbootefficientsearchapi.controller;

import com.mak.springbootefficientsearchapi.entity.Car;
import com.mak.springbootefficientsearchapi.entity.utils.PagingHeaders;
import com.mak.springbootefficientsearchapi.entity.utils.PagingResponse;
import com.mak.springbootefficientsearchapi.model.Vehicle;
import com.mak.springbootefficientsearchapi.service.CarService;
import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.pivovarit.function.ThrowingFunction.unchecked;


@Slf4j
@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    private final ModelMapper modelMapper;

    @Autowired
    public CarController(CarService carService, ModelMapper modelMapper) {
        this.carService = carService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Car create(@RequestBody Vehicle vehicle) {
        Car car = modelMapper.map(vehicle, Car.class);
        return carService.create(car);
    }

    @Transactional
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @CachePut(value = "carSearchCache", key = "#id")
    public Car update(@PathVariable(name = "id") Integer id, @RequestBody Vehicle vehicle) {
        Car car = modelMapper.map(vehicle, Car.class);
        return carService.update(id, car);
    }

    @Transactional
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = "carSearchCache", key = "#id")
    public void delete(@PathVariable(name = "id") Integer id) {
        carService.delete(id);
    }

    @Transactional
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Cacheable(value = "carSearchCache", key = "#id")
    public Car get(@PathVariable(name = "id") Integer id) {
        return carService.get(id);
    }

    @Transactional
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Cacheable(value = "carSearchCache", key = "{#root.methodName, #spec, #sort, #headers}")
    public ResponseEntity<List<Car>> get(
            @And({
                    @Spec(path = "manufacturer", params = "manufacturer", spec = Like.class),
                    @Spec(path = "model", params = "model", spec = Like.class),
                    @Spec(path = "country", params = "country", spec = In.class),
                    @Spec(path = "type", params = "type", spec = Like.class),
                    @Spec(path = "createDate", params = "createDate", spec = Equal.class),
                    @Spec(path = "createDate", params = {"createDateGt", "createDateLt"}, spec = Between.class)
            }) Specification<Car> spec,
            Sort sort,
            @RequestHeader HttpHeaders headers) {
        final PagingResponse response = carService.get(spec, headers, sort);
        return new ResponseEntity<>(response.getElements(), returnHttpHeaders(response), HttpStatus.OK);
    }

    public HttpHeaders returnHttpHeaders(PagingResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(PagingHeaders.COUNT.getName(), String.valueOf(response.getCount()));
        headers.set(PagingHeaders.PAGE_SIZE.getName(), String.valueOf(response.getPageSize()));
        headers.set(PagingHeaders.PAGE_OFFSET.getName(), String.valueOf(response.getPageOffset()));
        headers.set(PagingHeaders.PAGE_NUMBER.getName(), String.valueOf(response.getPageNumber()));
        headers.set(PagingHeaders.PAGE_TOTAL.getName(), String.valueOf(response.getPageTotal()));
        return headers;
    }

    @ResponseBody
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<List<Car>> uploadFile(@RequestParam(value = "files") List<MultipartFile> files) {
        List<Car> cars = files.stream()
                .parallel()
                .map(unchecked(carService::uploadFile))
                .flatMap(Collection::stream)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cars);
    }

    @GetMapping(value = "/extract", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Resource> extractFile(
            @And({
                    @Spec(path = "manufacturer", params = "manufacturer", spec = Like.class),
                    @Spec(path = "model", params = "model", spec = Like.class),
                    @Spec(path = "country", params = "country", spec = In.class),
                    @Spec(path = "type", params = "type", spec = Like.class),
                    @Spec(path = "createDate", params = "createDate", spec = Equal.class),
                    @Spec(path = "createDate", params = {"createDateGt", "createDateLt"}, spec = Between.class)
            }) Specification<Car> spec,
            Sort sort) throws IOException {
        List<Car> cars = carService.get(spec, sort);
        Resource resource = carService.generateCsvFile(cars);

        LocalDateTime now = LocalDateTime.now();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Search_Extraction_" + now + ".csv\"")
                .body(resource);
    }


}
