package com.oskarro.batcher.batch.synchronizeDatabase.service;

import com.oskarro.batcher.environment.backup.repo.SongRepository;
import org.springframework.transaction.annotation.Transactional;

public class BackupDatabaseService {

    SongRepository songRepository;

    public BackupDatabaseService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Transactional(transactionManager = "backupTransactionManager")
    public Long getNumberOfSongs() {
        long counter = songRepository.count();
        System.out.println("Currently number of songs: " + counter);
        return counter;
    }
}
