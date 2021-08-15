package com.oskarro.batcher.batch.itemReaders;

import com.oskarro.batcher.environment.main.model.cargo.Console;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class ConsoleFieldMapper implements FieldSetMapper<Console> {

    @Override
    public Console mapFieldSet(FieldSet fieldSet) throws BindException {
        Console console = new Console();
        console.setConsoleId(fieldSet.readInt("consoleId"));
        console.setModel(fieldSet.readString("model"));
        console.setProducer(fieldSet.readString("producer"));
        console.setPrice(fieldSet.readBigDecimal("price"));
        console.setProductStatus(fieldSet.readString("productStatus"));
        console.setOrderDate(fieldSet.readDate("orderDate"));
        return console;
    }
}
