package com.oskarro.batcher.batch.synchronizeDatabase.service;

import com.oskarro.batcher.environment.main.repo.TrackRepository;
import org.springframework.transaction.annotation.Transactional;

public class MainDatabaseService {

    TrackRepository trackRepository;

    public MainDatabaseService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Transactional(transactionManager = "mainTransactionManager")
    public Long getNumberOfRecords() {
        long counter = trackRepository.count();
        System.out.println("Currently number of records: " + counter);
        return counter;
    }

}
