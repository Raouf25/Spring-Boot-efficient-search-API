package com.mak.springbootefficientsearchapi.service;

import com.mak.springbootefficientsearchapi.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersonService extends GenericCsv<Person> {

    public PersonService() {
        super(Person.class);
    }

    public List<Person> parseEmail(List<Person> people) {
        return people.stream()
                     .filter(p -> p.getEmail().contains("@"))
                     .map(p -> new Person(p.getEmail().toLowerCase()))
                     .distinct()
                     .sorted(Comparator.comparing(Person::getEmail))
                     .collect(Collectors.toList());
    }


//        List<Person> collect = personList.stream()
//                                         .filter(person -> !person.getEmail().contains("commercial")
//                                                 && !person.getEmail().contains("info")
//                                                 && !person.getEmail().contains("contacts")
//                                                 && !person.getEmail().contains("@planet.tn")
//                                                 && !person.getEmail().contains("@live.fr")
//                                                 && !person.getEmail().contains("@gnet.tn")
//                                                 && !person.getEmail().contains("@gnet.e43tn")
//                                                 && !person.getEmail().contains("@hexabyte.tn")
//                                                 && !person.getEmail().contains("@gmail.com")
//                                                 && !person.getEmail().contains("@topnet.tn")
//                                                 && !person.getEmail().contains("@yahoo.fr")
//                                         )
//                                         .sorted(Comparator.comparing(Person::getEmail))
//                                         .collect(Collectors.toList());

//
//
//        List<Person> collect2 = personList.stream()
//                                          .filter(person -> person.getEmail().contains("@planet.tn")
//                                                  || person.getEmail().contains("@live.fr")
//                                                  || person.getEmail().contains("@gnet.tn")
//                                                  || person.getEmail().contains("@gnet.e43tn")
//                                                  || person.getEmail().contains("@hexabyte.tn")
//                                                  || person.getEmail().contains("@gmail.com")
//                                                  || person.getEmail().contains("@topnet.tn")
//                                                  || person.getEmail().contains("@yahoo.fr")
//                                          )
//                                          .sorted(Comparator.comparing(Person::getEmail))
//                                          .collect(Collectors.toList());

}
