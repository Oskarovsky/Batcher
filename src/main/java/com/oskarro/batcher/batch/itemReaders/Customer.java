package com.oskarro.batcher.batch.itemReaders;

import lombok.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class Customer {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zipCode;

    List<Transaction> transactions;

    @XmlElementWrapper(name = "transactions")
    @XmlElement(name = "transaction")
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

}
