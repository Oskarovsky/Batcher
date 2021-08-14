package com.oskarro.batcher.batch.synchronizeProducts;

import com.oskarro.batcher.environment.main.model.cargo.ProductItem;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.transform.FieldSet;

public class ProductItemReaderBean implements ProductItemReader<ProductItem> {

    private final ItemStreamReader<FieldSet> fieldSetReader;
    private int recordCount = 0;
    private int expectedRecordCount = 0;

    public ProductItemReaderBean(ItemStreamReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

    @Override
    public ProductItem read() throws Exception {
        return process(fieldSetReader.read());
    }

    public ProductItem process(FieldSet fieldSet) {
        ProductItem result = null;
        if (fieldSet != null) {
            if (fieldSet.getFieldCount() > 1) {
                result = new ProductItem();
                recordCount++;
            } else {
                expectedRecordCount = fieldSet.readInt(0);
            }
        }
        return result;
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
