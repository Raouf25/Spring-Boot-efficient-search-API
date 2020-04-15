package com.mak.springbootefficientsearchapi.service;

import com.mak.springbootefficientsearchapi.entity.Car;
import com.mak.springbootefficientsearchapi.repository.CarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

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
     * @return List of element
     * @throws EntityNotFoundException Exception when retrieve element
     */
    public List<Car> getAll() throws EntityNotFoundException {
        return (List<Car>) carRepository.findAll();
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

}
