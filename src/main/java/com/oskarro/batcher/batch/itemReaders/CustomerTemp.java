package com.oskarro.batcher.batch.itemReaders;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTemp {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zipCode;
}
