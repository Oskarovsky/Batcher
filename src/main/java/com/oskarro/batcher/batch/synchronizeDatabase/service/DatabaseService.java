package com.oskarro.batcher.batch.synchronizeDatabase.service;

import com.oskarro.batcher.model.Track;
import com.oskarro.batcher.repository.TrackRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

public class DatabaseService {

    TrackRepository trackRepository;

    public DatabaseService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public Long getNumberOfRecords() {
        long counter = trackRepository.count();
        System.out.println("Currently number of records: " + counter);
        return counter;
    }

}
