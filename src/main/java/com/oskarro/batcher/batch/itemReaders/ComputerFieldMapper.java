package com.oskarro.batcher.batch.itemReaders;

import com.oskarro.batcher.environment.main.model.cargo.Computer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class ComputerFieldMapper implements FieldSetMapper<Computer> {

    @Override
    public Computer mapFieldSet(FieldSet fieldSet) throws BindException {
        Computer computer = new Computer();
        computer.setComputerId(fieldSet.readInt("computerId"));
        computer.setName(fieldSet.readString("name"));
        computer.setModel(fieldSet.readString("model"));
        computer.setDescription(fieldSet.readString("description"));
        computer.setProductStatus(fieldSet.readString("productStatus"));
        computer.setOrderDate(fieldSet.readDate("orderDate"));
        return computer;
    }
}
