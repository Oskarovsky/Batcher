package com.oskarro.batcher.batch.itemReaders;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CustomerFieldSetMapper implements FieldSetMapper<CustomerTemp> {

    @Override
    public CustomerTemp mapFieldSet(FieldSet fieldSet) throws BindException {
        CustomerTemp customer = new CustomerTemp();
        customer.setAddress(fieldSet.readString("addressNumber") + " " + fieldSet.readString("street"));
        customer.setCity(fieldSet.readString("city"));
        customer.setFirstName(fieldSet.readString("firstName"));
        customer.setLastName(fieldSet.readString("lastName"));
        customer.setZipCode(fieldSet.readString("zipCode"));
        return null;
    }
}
