package com.oskarro.batcher.environment.main.repo;

import com.oskarro.batcher.environment.main.model.cargo.Computer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface ComputerRepository
        extends JpaRepository<Computer, Serializable>, JpaSpecificationExecutor<Computer> {

    long count();
}
