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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CarService extends GenericCsv<Car> {

    private final CarRepository carRepository;

    @Autowired
    public CarService(CarRepository carRepository) {
        super(Car.class);
        this.carRepository = carRepository;
    }

    /**
     * delete element
     *
     * @param id element ID
     * @throws EntityNotFoundException Exception when retrieve entity
     */
    public void delete(Integer id) {
        Car entity = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Can not find the entity car (%s = %s).", "id", id)));
        carRepository.delete(entity);
    }

    /**
     * @param id element ID
     * @return element
     * @throws EntityNotFoundException Exception when retrieve element
     */
    public Car get(Integer id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Can not find the entity car (%s = %s).", "id", id)));
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
            List<Car> entities = get(spec, sort);
            return new PagingResponse((long) entities.size(), 0L, 0L, 0L, 0L, entities);
        }
    }

    private boolean isRequestPaged(HttpHeaders headers) {
        return headers.containsKey(PagingHeaders.PAGE_NUMBER.getName()) && headers.containsKey(PagingHeaders.PAGE_SIZE.getName());
    }

    private Pageable buildPageRequest(HttpHeaders headers, Sort sort) {
        int page = Integer.parseInt(Objects.requireNonNull(headers.get(PagingHeaders.PAGE_NUMBER.getName())).get(0));
        int size = Integer.parseInt(Objects.requireNonNull(headers.get(PagingHeaders.PAGE_SIZE.getName())).get(0));
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
        Page<Car> page = carRepository.findAll(spec, pageable);
        List<Car> content = page.getContent();
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
        return carRepository.findById(id)
                .map(carFound -> {
                    item.setId(id);
                    return save(item);
                }).orElseThrow(()-> new EntityNotFoundException("Can not update entity, entity without ID."));
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


    @Async
    public List<Car> uploadFile(MultipartFile multipartFile) throws IOException {
        List<Car> carList = parseCsvFile(multipartFile);
        return (List<Car>) carRepository.saveAll(carList);
    }

}
