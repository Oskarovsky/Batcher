package com.oskarro.batcher.batch.itemReaders;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private String firstName;
    private String lastName;
    private String addressNumber;
    private String street;
    private String city;
    private String zipCode;

}
