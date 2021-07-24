package com.oskarro.batcher.batch.synchronizeProducts;

import com.oskarro.batcher.environment.main.dao.ComputerDao;
import com.oskarro.batcher.environment.main.model.cargo.Computer;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class ComputerApplierProcessor implements ItemProcessor<Computer, Computer> {

    private final ComputerDao computerDao;

    public ComputerApplierProcessor(ComputerDao computerDao) {
        this.computerDao = computerDao;
    }

    @Override
    public Computer process(Computer computer) throws Exception {
        computer.setProductStatus("IN-PROGRESS");
        Computer comp = computerDao.getComputerById(computer.getComputerId());
        return computer;
    }
}
