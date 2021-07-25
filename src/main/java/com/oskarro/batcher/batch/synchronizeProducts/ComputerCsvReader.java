package com.oskarro.batcher.batch.synchronizeProducts;

import com.oskarro.batcher.environment.main.model.cargo.Computer;
import com.oskarro.batcher.environment.main.model.cargo.Department;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.transform.FieldSet;

import java.util.concurrent.ThreadLocalRandom;


public class ComputerCsvReader implements ItemStreamReader<Computer> {

    private ItemStreamReader<FieldSet> fieldSetReader;
    private int recordCount = 0;
    private int expectedRecordCount = 0;

    public ComputerCsvReader(ItemStreamReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

    @Override
    public Computer read() throws Exception {
        return process(fieldSetReader.read());
    }

    private Computer process(FieldSet fieldSet) {
        Computer result = null;
        if (fieldSet != null) {
            if (fieldSet.getFieldCount() > 1) {
                result = new Computer();
                if (fieldSet.readString(0).equals("null")) {
                    result.setComputerId(ThreadLocalRandom.current().nextInt(1,1000000000));
                } else {
                    result.setComputerId(fieldSet.readInt(0));
                }
                result.setName(fieldSet.readString(1));
                result.setDescription(fieldSet.readString(2));
                result.setModel(fieldSet.readString(3));
                result.setPrice(fieldSet.readBigDecimal(4));
                result.setProductStatus(fieldSet.readString(5));
                result.setOrderDate(fieldSet.readDate(6));
                Department department = new Department();
                department.setDepartmentId(fieldSet.readInt(7));
                result.setDepartment(department);
                recordCount++;
            } else {
                expectedRecordCount = fieldSet.readInt(0);
            }
        }
        return result;
    }

    public void setFieldSetReader(ItemStreamReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution execution) {
        if (recordCount == expectedRecordCount) {
            return execution.getExitStatus();
        } else {
            return ExitStatus.STOPPED;
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        this.fieldSetReader.close();
    }
}
