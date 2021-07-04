package com.oskarro.batcher.batch.synchronizeDatabase.service;

import com.oskarro.batcher.environment.backup.model.Song;
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
        System.out.println("[BACKUP] Currently number of items: " + counter);
        return counter;
    }

    @Transactional(transactionManager = "backupTransactionManager")
    public void validateSong(String code) {
        Song song = songRepository.findByCode(code);
        if (song != null) {
            System.out.printf("Track with code %s already exists in SONG_REPO%n", code);
        } else {
            System.out.printf("Track with code %s doesn't exist in SONG_REPO%n", code);
        }
    }
}
