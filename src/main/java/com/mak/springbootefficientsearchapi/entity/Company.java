package com.mak.springbootefficientsearchapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    private String name;
    private String phone;
    private String email;
    private String website;
    private String address;
}
