package com.oskarro.batcher.batch.itemReaders;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zipCode;

    List<Transaction> transactionList;

}
