package com.mak.springbootefficientsearchapi;


import com.mak.springbootefficientsearchapi.entity.Company;
import com.mak.springbootefficientsearchapi.entity.Person;
import com.mak.springbootefficientsearchapi.service.CompanyService;
import com.mak.springbootefficientsearchapi.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootApplication
public class SpringBootEfficientSearchApiApplication implements CommandLineRunner {

    @Autowired
    private PersonService personService;

    @Autowired
    private CompanyService companyService;


    public static void main(String[] args) {
        SpringApplication.run(SpringBootEfficientSearchApiApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        File fileCompany = ResourceUtils.getFile("classpath:static/Company.csv");
        List<Company> companyList = companyService.init(fileCompany);
        List<String> emailList = companyList.stream().map(Company::getEmail).collect(Collectors.toList());

        MultipartFile multipartFile = new MockMultipartFile("test.xlsx", new FileInputStream(ResourceUtils.getFile("classpath:static/Person.csv")));
        List<Person> people = personService.parseCsvFile(multipartFile);


        List<Person> other = people.stream()
                                   .filter(person -> !emailList.contains(person.getEmail()))
                                   .collect(Collectors.toList());
        other.forEach(o -> System.out.println(o.getEmail()));
    }
}
