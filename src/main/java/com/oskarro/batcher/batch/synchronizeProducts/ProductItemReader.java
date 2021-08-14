package com.oskarro.batcher.batch.synchronizeProducts;

import com.oskarro.batcher.environment.main.model.cargo.ProductItem;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.transform.FieldSet;

public interface ProductItemReader <T extends ProductItem> extends ItemStreamReader<T> {

    T read() throws Exception;

    T process(FieldSet fieldSet);

    ExitStatus afterStep(StepExecution execution);

    void open(ExecutionContext executionContext) throws ItemStreamException;

    void update(ExecutionContext executionContext) throws ItemStreamException;

    void close() throws ItemStreamException;
}
