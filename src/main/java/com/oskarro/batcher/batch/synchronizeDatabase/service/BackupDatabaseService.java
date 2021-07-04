package com.oskarro.batcher.batch.synchronizeDatabase.service;

import com.oskarro.batcher.environment.backup.model.Song;
import com.oskarro.batcher.environment.backup.repo.SongRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public String validateSong(String code) {
        List<Song> song = songRepository.findAllByCode(code);
        if (song.size() == 1) {
//            System.out.printf("Track with code %s already exists in SONG_REPO%n", code);
            return "UPDATE";
        } else if (song.size() > 1) {
//            System.out.printf("There are too much results for code %s %n", code);
            return "DELETE";
        } else {
//            System.out.printf("Track with code %s doesn't exist in SONG_REPO%n", code);
            return "INSERT";
        }
    }
}
