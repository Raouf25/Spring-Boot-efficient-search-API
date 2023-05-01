package com.mak.springbootefficientsearchapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mak.springbootefficientsearchapi.entity.Car;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.Assert.notNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CarTest {

    private ResultActions result;

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        notNull(webClient, "MockMVC can not be null");

        webClient = MockMvcBuilders
                .webAppContextSetup(context)
                //     .apply(springSecurity()) // enable security for the mock set up
                .build();
    }

    @Test
    void should_return_car_when_get_car_by_id() throws Exception {
        result = webClient.perform(get("/api/cars/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());

        Car car = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), Car.class);

        assertThat(car.getId()).isEqualTo(1);
        assertThat(car.getManufacturer()).isEqualTo("Acura");
        assertThat(car.getModel()).isEqualTo("Integra");
        assertThat(car.getType()).isEqualTo("Small");
        assertThat(car.getCountry()).isEqualTo("Japan");
        assertThat(car.getCreateDate()).isEqualTo(LocalDate.parse("1931-02-01", DateTimeFormatter.ofPattern("yyyy-MM-d")));
    }

    @Test
    void should_return_car_list_when_get_car_sorted_by_createDate_in_ASC_order() throws Exception {
        result = webClient.perform(get("/api/cars?sort=createDate,asc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());

        List<Car> carList = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), new TypeReference<List<Car>>() {
        });

        Assertions.assertThat(carList)
                .hasSize(192)
                .extracting(Car::getCreateDate)
                .map(LocalDate::toString)
                .contains("1931-02-01", "2016-06-18","2016-09-13", "2017-02-03", "2017-02-04");

    }

    @Test
    void should_return_car_list_when_get_car_for_specific_criteria_sorted_by_createDate_in_DESC_order() throws Exception {
        result = webClient.perform(get("/api/cars?country=Japan&type=Small&sort=createDate,desc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());

        List<Car> carList = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), new TypeReference<List<Car>>() {
        });

        assertTrue(carList.stream().allMatch(car -> car.getCountry().equals("Japan") && car.getType().equals("Small")));

        Assertions.assertThat(carList)
                .hasSize(1)
                .extracting(Car::getCreateDate)
                .map(LocalDate::toString)
                .contains("1931-02-01");
    }

    @Test
    void should_return_car_list_page_when_get_car_for_specific_criteria_sorted_by_createDate_in_DESC_order() throws Exception {
        result = webClient.perform(get("/api/cars?country=Germany&type=Sedan&sort=createDate,desc")
                        .header("Page-Number", 1)
                        .header("Page-Size", 10))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());


        List<Car> carList = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), new TypeReference<List<Car>>() {
        });

        assertThat(result.andReturn().getResponse().getHeader("count")).isEqualTo("15");
        assertThat(result.andReturn().getResponse().getHeader("page-Number")).isEqualTo("1");
        assertThat(result.andReturn().getResponse().getHeader("page-Size")).isEqualTo("5");
        assertThat(result.andReturn().getResponse().getHeader("page-Offset")).isEqualTo("10");
        assertThat(result.andReturn().getResponse().getHeader("page-Total")).isEqualTo("2");

        assertTrue(carList.stream().allMatch(car -> car.getCountry().equals("Germany") && car.getType().equals("Sedan")));

        List<LocalDate> actualCreatedDateList = carList.stream().map(Car::getCreateDate).toList();
        assertThat(actualCreatedDateList.toString()).hasToString("[2018-07-09, 2018-02-13, 2017-07-21, 2017-07-17, 2017-02-04]");
    }

}


