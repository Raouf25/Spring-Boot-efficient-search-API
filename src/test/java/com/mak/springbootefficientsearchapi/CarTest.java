package com.mak.springbootefficientsearchapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mak.springbootefficientsearchapi.entity.Car;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
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
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.Assert.notNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CarTest {

    private ResultActions result;

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @Before
    public void setup() {
        notNull(webClient, "MockMVC can not be null");

        webClient = MockMvcBuilders
                .webAppContextSetup(context)
                //     .apply(springSecurity()) // enable security for the mock set up
                .build();
    }

    @Test
    public void should_return_car_when_get_car_by_id() throws Exception {
        result = webClient.perform(get("/api/cars/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());

        Car car = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), Car.class);

        assertThat(car.getId()).isEqualTo(1);
        assertThat(car.getManufacturer()).isEqualTo("Acura");
        assertThat(car.getModel()).isEqualTo("Integra");
        assertThat(car.getType()).isEqualTo("Small");
        assertThat(car.getCountry()).isEqualTo("Japon");
        assertThat(car.getCreateDate()).isEqualTo(LocalDate.parse("1931-02-01", DateTimeFormatter.ofPattern("yyyy-MM-d")));
    }

    @Test
    public void should_return_car_list_when_get_car_sorted_by_createDate_in_ASC_order() throws Exception {
        result = webClient.perform(get("/api/cars?sort=createDate,asc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());

        List<Car> carList = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), new TypeReference<List<Car>>() {
        });

        List<LocalDate> actualSortedList = carList.stream().map(Car::getCreateDate).collect(Collectors.toList());
        assertThat(actualSortedList.toString()).hasToString("[1928-01-26, 1928-02-05, 1928-03-02, 1928-04-25, 1929-07-20, 1929-10-09, 1929-10-09, 1929-11-04, 1929-11-04, 1930-10-06, 1930-12-02, 1931-02-01, 1931-09-08, 1932-02-26, 1932-03-20, 1932-07-05, 1933-11-05, 1934-02-27, 1935-01-27, 1935-05-12, 1936-10-31, 1938-05-06, 1938-05-30, 1938-06-26, 1939-12-20, 1940-02-06, 1940-02-25, 1940-03-04, 1940-05-27, 1940-06-13, 1940-06-18, 1940-08-04, 1941-05-12, 1941-05-24, 1942-04-25, 1943-06-02, 1943-10-19, 1944-11-02, 1945-09-27, 1946-06-12, 1946-10-14, 1947-05-06, 1947-06-26, 1947-10-08, 1948-05-18, 1949-02-21, 1949-06-07, 1949-07-19, 1950-01-27, 1950-04-19, 1950-10-28, 1951-01-11, 1951-08-10, 1952-06-18, 1953-02-06, 1953-02-26, 1953-03-24, 1954-01-27, 1954-05-24, 1954-06-12, 1955-06-08, 1955-07-07, 1956-02-01, 1956-12-01, 1957-02-20, 1957-07-30, 1957-09-26, 1957-12-07, 1958-02-19, 1958-11-09, 1959-05-18, 1959-07-30, 1959-09-11, 1960-07-07, 1960-11-04, 1961-02-19, 1962-02-28, 1962-06-07, 1962-08-02, 1963-09-17, 1963-10-04, 1964-04-10, 1964-07-10, 1964-10-22, 1965-01-09, 1965-10-30, 1966-05-20, 1966-11-01, 1967-09-16, 1968-07-23, 1968-10-23, 1970-07-30, 1970-08-17]");
    }

    @Test
    public void should_return_car_list_when_get_car_for_specific_criteria_sorted_by_createDate_in_DESC_order() throws Exception {
        result = webClient.perform(get("/api/cars?country=USA&type=Small&sort=createDate,desc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());

        List<Car> carList = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), new TypeReference<List<Car>>() {
        });

        assertTrue(carList.stream().allMatch(car -> car.getCountry().equals("USA") && car.getType().equals("Small")));

        List<LocalDate> actualCreatedDateList = carList.stream().map(Car::getCreateDate).collect(Collectors.toList());
        assertThat(actualCreatedDateList.toString()).hasToString("[1965-10-30, 1964-10-22, 1954-01-27, 1953-02-06, 1943-06-02, 1941-05-12, 1934-02-27, 1930-12-02]");
    }

    @Test
    public void should_return_car_list_page_when_get_car_for_specific_criteria_sorted_by_createDate_in_DESC_order() throws Exception {
        result = webClient.perform(get("/api/cars?country=USA&type=Small&sort=createDate,desc")
                        .header("Page-Number", 1)
                        .header("Page-Size", 5))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());


        List<Car> carList = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), new TypeReference<List<Car>>() {
        });

        assertThat(result.andReturn().getResponse().getHeader("count")).isEqualTo("8");
        assertThat(result.andReturn().getResponse().getHeader("page-Number")).isEqualTo("1");
        assertThat(result.andReturn().getResponse().getHeader("page-Size")).isEqualTo("3");
        assertThat(result.andReturn().getResponse().getHeader("page-Offset")).isEqualTo("5");
        assertThat(result.andReturn().getResponse().getHeader("page-Total")).isEqualTo("2");

        assertTrue(carList.stream().allMatch(car -> car.getCountry().equals("USA") && car.getType().equals("Small")));

        List<LocalDate> actualCreatedDateList = carList.stream().map(Car::getCreateDate).collect(Collectors.toList());
        assertThat(actualCreatedDateList.toString()).hasToString("[1941-05-12, 1934-02-27, 1930-12-02]");
    }

}


