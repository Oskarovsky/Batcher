package com.oskarro.batcher.environment.main.repo;

import com.oskarro.batcher.environment.main.model.cargo.Console;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface ConsoleRepository
        extends JpaRepository<Console, Serializable>, JpaSpecificationExecutor<Console> {

    long count();

}
