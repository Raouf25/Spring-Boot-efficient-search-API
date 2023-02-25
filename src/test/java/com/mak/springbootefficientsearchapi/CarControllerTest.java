package com.mak.springbootefficientsearchapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mak.springbootefficientsearchapi.controller.CarController;
import com.mak.springbootefficientsearchapi.entity.Car;
import com.mak.springbootefficientsearchapi.service.CarService;
import com.mak.springbootefficientsearchapi.util.Builder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class CarControllerTest {

    @Mock
    private CarService carService;
    private Car car;
    @InjectMocks
    private CarController carController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        car = Builder.car(1, "WALLYS", "IRIS", "Small", "Tunisia", LocalDate.of(2006, 8, 29));
        mockMvc = MockMvcBuilders.standaloneSetup(carController).build();
    }

    @AfterEach
    void tearDown() {
        car = null;
    }

    @Test
    void testUploadFile() throws Exception {
        // Create a list of MultipartFiles
        MockMultipartFile file1 = new MockMultipartFile("files", "file1.csv", MediaType.TEXT_PLAIN_VALUE, "file1".getBytes());

        // Create a list of Car objects
        List<Car> cars = Arrays.asList(
                new Car(1, "Toyota", "Corolla", "Japan", "Sedan", LocalDate.of(2020, 1, 1)),
                new Car(2, "Honda", "Civic", "Japan", "Sedan", LocalDate.of(2019, 6, 1)),
                new Car(3, "Tesla", "Model 3", "USA", "Sedan", LocalDate.of(2021, 3, 1))
        );

        // Mock the car service to return the list of Car objects when uploadFile is called
        when(carService.uploadFile(any(MultipartFile.class))).thenReturn(cars);

        // Send a POST request to the API with the list of MultipartFiles as a parameter
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/cars/upload")
                        .file(file1))
                .andExpect(status().isCreated())
                .andReturn();

        // Verify the response
        String json = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        List<Car> actualCars = objectMapper.readValue(json, new TypeReference<>() {
        });
        Assertions.assertThat(cars).usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(actualCars);
    }

    @Test
    void PostMappingOfCar() throws Exception {
        when(carService.create(any())).thenReturn(car);

        mockMvc.perform(post("/api/cars").
                        contentType(MediaType.APPLICATION_JSON).
                        content(asJsonString(car))).
                andExpect(status().isCreated());

        verify(carService, times(1)).create(any());
    }


    @Test
    void GetMappingOfCar() throws Exception {
        when(carService.get(any())).thenReturn(car);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cars/1").
                        contentType(MediaType.APPLICATION_JSON).
                        content(asJsonString(car))).
                andExpect(status().isOk());

        verify(carService, times(1)).get(any());
    }


    @Test
    void DeleteMappingOfCar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cars/1").
                        contentType(MediaType.APPLICATION_JSON).
                        content(asJsonString(car))).
                andExpect(status().isNoContent());

        verify(carService, times(1)).delete(any());
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
