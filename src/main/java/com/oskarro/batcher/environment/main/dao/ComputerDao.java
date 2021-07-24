package com.oskarro.batcher.environment.main.dao;

import com.oskarro.batcher.environment.main.model.cargo.Computer;

import java.util.List;

public interface ComputerDao {

    List<Computer> getComputersByModel(String model);
}
