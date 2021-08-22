package com.oskarro.batcher.batch.itemReaders;

import org.springframework.batch.item.*;

import java.util.ArrayList;

public class CustomerFileReader implements ItemStreamReader<Customer> {

    private Object curItem = null;
    private final ItemStreamReader<Object> delegate;

    public CustomerFileReader(ItemStreamReader<Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (curItem == null) {
            curItem = delegate.read();
        }
        Customer item = (Customer) curItem;
        curItem = null;

        if (item != null) {
            item.setTransactions(new ArrayList<>());

            while(peek() instanceof Transaction) {
                item.getTransactions().add((Transaction) curItem);
                curItem = null;
            }
        }
        return item;
    }


    private Object peek() throws Exception {
        if (curItem == null) {
            curItem = delegate.read();
        }
        return curItem;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }
}
