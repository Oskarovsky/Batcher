package com.oskarro.batcher.batch.synchronizeDatabase.service;

import com.oskarro.batcher.environment.main.model.Track;
import com.oskarro.batcher.environment.main.repo.ComputerRepository;
import com.oskarro.batcher.environment.main.repo.ConsoleRepository;
import com.oskarro.batcher.environment.main.repo.SmartphoneRepository;
import com.oskarro.batcher.environment.main.repo.TrackRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainDatabaseService {

    private static final int FETCH_SIZE = 10000;

    private final TrackRepository trackRepository;
    private final ComputerRepository computerRepository;
    private final ConsoleRepository consoleRepository;
    private final SmartphoneRepository smartphoneRepository;

    public MainDatabaseService(final TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
        this.computerRepository = null;
        this.smartphoneRepository = null;
        this.consoleRepository = null;
    }

    public MainDatabaseService(final ComputerRepository computerRepository,
                               final ConsoleRepository consoleRepository,
                               final SmartphoneRepository smartphoneRepository) {
        this.computerRepository = computerRepository;
        this.consoleRepository = consoleRepository;
        this.smartphoneRepository = smartphoneRepository;
        this.trackRepository = null;

    }

    @Transactional(transactionManager = "mainTransactionManager")
    public Long getNumberOfRecords() {
        long counter = trackRepository.count();
        System.out.println("[PROD] Currently number of items: " + counter);
        if (counter == 0) {
            throw new RuntimeException("There are no items in main database");
        }
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
