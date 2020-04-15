package com.mak.springbootefficientsearchapi.repository;

import com.mak.springbootefficientsearchapi.entity.Car;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends CrudRepository<Car, Integer> {
}
