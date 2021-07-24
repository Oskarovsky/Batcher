package com.oskarro.batcher.batch.synchronizeProducts;

import com.oskarro.batcher.environment.main.dao.ComputerDao;
import com.oskarro.batcher.environment.main.model.cargo.Computer;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class ComputerOrderProcessor implements ItemProcessor<Computer, Computer> {

    private ComputerDao computerDao;

    public ComputerOrderProcessor(ComputerDao computerDao) {
        this.computerDao = computerDao;
    }

    @Override
    public Computer process(Computer computer) throws Exception {
        List<Computer> computerList = computerDao.getComputersByModel(computer.getModel());

        for (Computer comp : computerList) {
            computer.setProductStatus("IN-PROGRESS");
        }
        return computer;
    }
}
