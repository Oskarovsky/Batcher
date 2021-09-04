package com.oskarro.batcher.batch.itemProcessor;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import java.util.HashSet;
import java.util.Set;

public class UniqueLastNameValidator extends ItemStreamSupport implements Validator<Client> {

    private Set<String> lastNames = new HashSet<>();

    @Override
    public void validate(Client client) throws ValidationException {
        if (lastNames.contains(client.getLastName())) {
            throw new ValidationException("Duplicate last name was found: " + client.getLastName());
        }
        this.lastNames.add(client.getLastName());
    }

    @Override
    public void open(ExecutionContext executionContext) {
        String lastNames = getExecutionContextKey("lastNames");
        if (executionContext.containsKey(lastNames)) {
            this.lastNames = (Set<String>) executionContext.get(lastNames);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        executionContext.put(getExecutionContextKey("lastNames"), this.lastNames);
    }
}
