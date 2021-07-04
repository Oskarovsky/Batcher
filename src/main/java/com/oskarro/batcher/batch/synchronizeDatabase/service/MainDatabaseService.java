package com.oskarro.batcher.batch.synchronizeDatabase.service;

import com.oskarro.batcher.environment.main.model.Track;
import com.oskarro.batcher.environment.main.repo.TrackRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainDatabaseService {

    private static final int FETCH_SIZE = 10000;

    private final TrackRepository trackRepository;

    public MainDatabaseService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Transactional(transactionManager = "mainTransactionManager")
    public Long getNumberOfRecords() {
        long counter = trackRepository.count();
        System.out.println("[PROD] Currently number of items: " + counter);
        return counter;
    }

    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    public List<Track> processTracks() throws Exception {
        try(Stream<Track> trackStream = trackRepository.getAll()){
            List<Track> tracksList = new ArrayList<>();
            trackStream.forEach(tracksList::add);
            return tracksList;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new Exception("Error has been occurred during processing tracks");
        }
    }

}
