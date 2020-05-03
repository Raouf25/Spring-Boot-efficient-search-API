package com.mak.springbootefficientsearchapi.service;

import com.mak.springbootefficientsearchapi.entity.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompanyService extends GenericCsv<Company> {

    public CompanyService( ) {
        super(Company.class);
    }

    public List<Company> init(File file) throws IOException {
        long header = 1L;
        List<Company> personList = Files.lines(file.toPath(), Charset.forName("ISO-8859-1"))
                                        .parallel()
                                        .skip(header)
                                        .filter(line -> line.contains("@"))
                                        .map(String::toLowerCase)
                                        .map(line -> line.split(";"))
                                        .filter(tab -> tab.length >= 5)
                                        .map(getCompanyFunction())
                                        .distinct()
//                                        .sorted(Comparator.comparing(Person::getEmail))
                                        .collect(Collectors.toList());
        log.info("personList.size(): " + personList.size());
        return personList;
    }

    private Function<String[], Company> getCompanyFunction() {
        return data -> {
            Company company = new Company();
            company.setName(data[0]);
            company.setPhone(data[1]);
            company.setEmail(data[2]);
            company.setWebsite(data[3]);
            company.setAddress(data[4]);


            if (company.getWebsite().contains("@")) {
                company.setName(data[0]);
                company.setPhone(data[4]);
                company.setEmail(data[3]);
                company.setWebsite(data[2]);
                company.setAddress(data[1]);
            }
            return company;
        };
    }


}
