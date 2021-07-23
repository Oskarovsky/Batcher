package com.oskarro.batcher.environment.main.repo;

import com.oskarro.batcher.environment.main.model.cargo.Smartphone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface SmartphoneRepository
        extends JpaRepository<Smartphone, Serializable>, JpaSpecificationExecutor<Smartphone> {

    long count();
}
