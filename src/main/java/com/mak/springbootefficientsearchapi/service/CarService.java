package com.mak.springbootefficientsearchapi.service;

import com.mak.springbootefficientsearchapi.entity.Car;
import com.mak.springbootefficientsearchapi.entity.utils.PagingHeaders;
import com.mak.springbootefficientsearchapi.entity.utils.PagingResponse;
import com.mak.springbootefficientsearchapi.repository.CarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CarService {

    private final CarRepository carRepository;

    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * delete element
     *
     * @param id element ID
     * @throws EntityNotFoundException Exception when retrieve entity
     */
    public void delete(Integer id) throws EntityNotFoundException {
        Car entity = carRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException(String.format("Can not find the entity car (%s = %s).", "id", id.toString())));
        carRepository.delete(entity);
    }

    /**
     * @param id element ID
     * @return element
     * @throws EntityNotFoundException Exception when retrieve element
     */
    public Car get(Integer id) throws EntityNotFoundException {
        return carRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException(String.format("Can not find the entity car (%s = %s).", "id", id.toString())));
    }

    /**
     * get element using Criteria.
     *
     * @param spec    *
     * @param headers pagination data
     * @param sort    sort criteria
     * @return retrieve elements with pagination
     */
    public PagingResponse get(Specification<Car> spec, HttpHeaders headers, Sort sort) {
        if (isRequestPaged(headers)) {
            return get(spec, buildPageRequest(headers, sort));
        } else {
            final List<Car> entities = get(spec, sort);
            return new PagingResponse((long) entities.size(), 0L, 0L, 0L, 0L, entities);
        }
    }

    private boolean isRequestPaged(HttpHeaders headers) {
        return headers.containsKey(PagingHeaders.PAGE_NUMBER.getName()) && headers.containsKey(PagingHeaders.PAGE_SIZE.getName());
    }

    private Pageable buildPageRequest(HttpHeaders headers, Sort sort) {
        final int page = Integer.parseInt(headers.get(PagingHeaders.PAGE_NUMBER.getName()).get(0));
        final int size = Integer.parseInt(headers.get(PagingHeaders.PAGE_SIZE.getName()).get(0));
        return PageRequest.of(page, size, sort);
    }

    /**
     * get elements using Criteria.
     *
     * @param spec     *
     * @param pageable pagination data
     * @return retrieve elements with pagination
     */
    public PagingResponse get(Specification<Car> spec, Pageable pageable) {
        final Page<Car> page = carRepository.findAll(spec, pageable);
        final List<Car> content = page.getContent();
        return new PagingResponse(page.getTotalElements(), (long) page.getNumber(), (long) page.getNumberOfElements(), pageable.getOffset(), (long) page.getTotalPages(), content);
    }

    /**
     * get elements using Criteria.
     *
     * @param spec *
     * @return elements
     */
    public List<Car> get(Specification<Car> spec, Sort sort) {
        return carRepository.findAll(spec, sort);
    }

    /**
     * create element.
     *
     * @param item element to create
     * @return element after creation
     * //     * @throws CreateWithIdException   Exception lancée lors de la création d'un objet existant
     * @throws EntityNotFoundException Exception lors de récupération de l'entité en base
     */
    public Car create(Car item) {
        return save(item);
    }

    /**
     * update element
     *
     * @param id   element identifier
     * @param item element to update
     * @return element after update
     * @throws EntityNotFoundException Exception when retrieve entity
     */
    public Car update(Integer id, Car item) {
        if (item.getId() == null) {
            throw new RuntimeException("Can not update entity, entity without ID.");
        } else if (!id.equals(item.getId())) {
            throw new RuntimeException(String.format("Can not update entity, the resource ID (%d) not match the objet ID (%d).", id, item.getId()));
        }
        return save(item);
    }

    /**
     * create \ update elements
     *
     * @param item element to save
     * @return element after save
     */
    protected Car save(Car item) {
        return carRepository.save(item);
    }

    public void init(File file) throws IOException {
        long header = 1L;
        List<Car> carList = Files.lines(file.toPath())
                                 .parallel()
                                 .skip(header)
                                 .map(transformLineToCar())
                                 .collect(Collectors.toList());
        carRepository.saveAll(carList);
    }

    private Function<String, Car> transformLineToCar() {
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
