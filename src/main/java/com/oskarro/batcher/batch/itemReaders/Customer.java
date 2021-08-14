package com.oskarro.batcher.batch.itemReaders;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Customer {

    private String firstName;
    private String lastName;
    private String addressNumber;
    private String street;
    private String city;
    private String zipCode;

    public Customer(String firstName, String lastName, String addressNumber, String street, String city, String zipCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.addressNumber = addressNumber;
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
    }
}
