package com.mak.springbootefficientsearchapi;

import com.mak.springbootefficientsearchapi.entity.Car;
import com.mak.springbootefficientsearchapi.entity.utils.PagingHeaders;
import com.mak.springbootefficientsearchapi.entity.utils.PagingResponse;
import com.mak.springbootefficientsearchapi.repository.CarRepository;
import com.mak.springbootefficientsearchapi.service.CarService;
import com.mak.springbootefficientsearchapi.util.Builder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

	@Test
	void get_should_throw_EntityNotFoundException() throws EntityNotFoundException {
		assertThrows(
				EntityNotFoundException.class,
				() -> carService.get(1),
				"Expected doThing() to throw, but it didn't"
		);
	}

    @Test
    void get_should_return_the_entity() {
        // Given
        when(carRepository.findById(1)).thenReturn(Optional.of(Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29))));

        // When
        Car foundCar = carService.get(1);

        // Then
        assertThat(1, equalTo(foundCar.getId()));
        assertThat("WALLYS", equalTo(foundCar.getManufacturer()));
        assertThat("IRIS", equalTo(foundCar.getModel()));
        assertThat("Small", equalTo(foundCar.getType()));
        assertThat("Tunisia", equalTo(foundCar.getCountry()));
        assertThat("2006-08-29", equalTo(foundCar.getCreateDate().toString()));
    }

    @Test
    void get_by_specification_should_return_entities_list() {
        // Given
        Specification querySpec = mock(Specification.class);
        Sort sort = mock(Sort.class);
        when(carRepository.findAll(querySpec, sort))
                .thenReturn(
                        Arrays.asList(Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29)),
                                Builder.car(2, "Honda", "Civic", "Small", "Japan", LocalDate.of(1967, 9, 16))));

        // When
        List<Car> foundCar = carService.get(querySpec, sort);

        // Then
        verify(carRepository, times(1)).findAll(querySpec, sort);
        assertThat(2, equalTo(foundCar.size()));
        assertThat(1, equalTo(foundCar.get(0).getId()));
        assertThat("WALLYS", equalTo(foundCar.get(0).getManufacturer()));
        assertThat("IRIS", equalTo(foundCar.get(0).getModel()));
        assertThat("Small", equalTo(foundCar.get(0).getType()));
        assertThat("Tunisia", equalTo(foundCar.get(0).getCountry()));
        assertThat(LocalDate.of(2006, 8, 29), equalTo(foundCar.get(0).getCreateDate()));
        assertThat(2, equalTo(foundCar.get(1).getId()));
        assertThat("Honda", equalTo(foundCar.get(1).getManufacturer()));
        assertThat("Civic", equalTo(foundCar.get(1).getModel()));
        assertThat("Small", equalTo(foundCar.get(1).getType()));
        assertThat("Japan", equalTo(foundCar.get(1).getCountry()));
        assertThat(LocalDate.of(1967, 9, 16), equalTo(foundCar.get(1).getCreateDate()));
    }

    @Test
    void get_by_specification_and_pagination_should_return_paged_entities_list() {
        // Given
        Specification querySpec = mock(Specification.class);
        Sort sort = mock(Sort.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(PagingHeaders.PAGE_NUMBER.getName(), String.valueOf(0));
        headers.add(PagingHeaders.PAGE_SIZE.getName(), String.valueOf(2));

        PageRequest pageRequest = PageRequest.of(0, 2, sort);

        Page<Car> cars = Mockito.mock(Page.class);
        when(carRepository.findAll(any(), (Pageable) any())).thenReturn(cars);

        // When
        carService.get(querySpec, headers, sort);

        // Then
        verify(carRepository, times(1)).findAll(querySpec, pageRequest);
    }

    @Test
    void get_by_specification_and_sort_should_return_entities_list() {
        // Given
        Specification querySpec = mock(Specification.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(PagingHeaders.COUNT.getName(), String.valueOf(0));
        Sort sort = mock(Sort.class);

        List<Car> carList = Arrays.asList(
                Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29)),
                Builder.car(2, "Honda", "Civic", "Small", "Japan", LocalDate.of(1967, 9, 16)),
                Builder.car(3, "Ford", "Escort", "Small", "USA", LocalDate.of(1930, 12, 02)));

        when(carRepository.findAll(any(), (Sort) any())).thenReturn(carList);

        // When
        PagingResponse sortedList = carService.get(querySpec, headers, sort);

        // Then
        verify(carRepository, times(1)).findAll(querySpec, sort);
        assertThat(0L, equalTo(sortedList.getPageNumber()));
        assertThat(0L, equalTo(sortedList.getPageSize()));
        assertThat(0L, equalTo(sortedList.getPageTotal()));
        assertThat(3, equalTo(sortedList.getElements().size()));
    }

    @Test
    void create_should_return_saved_entity() {
        // Given
        Car car = Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29));

        when(carRepository.save(any())).thenReturn(car);
        ArgumentCaptor<Car> argument = ArgumentCaptor.forClass(Car.class);

        // When
        final Car createdCar = carService.create(car);

        // Then
        verify(carRepository, times(1)).save(argument.capture());
        assertThat(1, equalTo(createdCar.getId()));
        assertThat("WALLYS", equalTo(createdCar.getManufacturer()));
        assertThat("IRIS", equalTo(createdCar.getModel()));
        assertThat("Small", equalTo(createdCar.getType()));
        assertThat("Tunisia", equalTo(createdCar.getCountry()));
        assertThat("2006-08-29", equalTo(createdCar.getCreateDate().toString()));
    }

    @Test
    void update_should_return_updated_entity() {
        // Given
        Car car = Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29));

        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(car);
        ArgumentCaptor<Car> argument = ArgumentCaptor.forClass(Car.class);

        // When
        Car updatedCar = carService.update(1, car);

        // Then
        verify(carRepository, times(1)).save(argument.capture());
        assertThat(1, equalTo(updatedCar.getId()));
        assertThat("WALLYS", equalTo(updatedCar.getManufacturer()));
        assertThat("IRIS", equalTo(updatedCar.getModel()));
        assertThat("Small", equalTo(updatedCar.getType()));
        assertThat("Tunisia", equalTo(updatedCar.getCountry()));
        assertThat("2006-08-29", equalTo(updatedCar.getCreateDate().toString()));
    }

	@Test
	void save_should_throws_UpdateIdMismatchException() {
        Car car = Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29));
        assertThrows(
                RuntimeException.class,
                () -> carService.update(2, car),
                "Expected doThing() to throw, but it didn't"
        );
	}

	@Test
	void save_should_throws_Exception() {
        Car car = Builder.car(null, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29));
        assertThrows(
                RuntimeException.class,
                () -> carService.update(2,car),
                "Expected doThing() to throw, but it didn't"
        );
	}

    @Test
    void delete() {
        // Given
        when(carRepository.findById(1)).thenReturn(Optional.of(Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29))));
        ArgumentCaptor<Car> argumentCaptor = ArgumentCaptor.forClass(Car.class);

        // When
        carService.delete(1);

        // Then
        verify(carRepository, times(1)).delete(argumentCaptor.capture());
        assertThat(1, equalTo(argumentCaptor.getValue().getId()));
    }

    @Test
    void upload_csv_file_should_parse_csv_file() throws IOException {
        // Given
        MultipartFile multipartFile = new MockMultipartFile("car_file.csv", new FileInputStream(new File("src/test/resources/car_file.csv")));
        List<Car> cars = new ArrayList<>();
        ArgumentCaptor<List<Car>> carArgumentCaptor = ArgumentCaptor.forClass(cars.getClass());

        // When
        carService.uploadFile(multipartFile);

        // Then
        verify(carRepository, times(1)).saveAll(carArgumentCaptor.capture());
        assertThat(5, equalTo(carArgumentCaptor.getValue().size()));
    }

    @Test
    void extract_csv_file_should_create_resource() throws IOException {
        // Given
        List<Car> carList = Arrays.asList(
                Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29)),
                Builder.car(2, "Honda", "Civic", "Small", "Japan", LocalDate.of(1967, 9, 16)),
                Builder.car(3, "Ford", "Escort", "Small", "USA", LocalDate.of(1930, 12, 02)));

        // When
        Resource resource = carService.generateCsvFile(carList);

        // Then
        assertThat(true, equalTo(resource.exists()));
    }

}
