package com.oskarro.batcher.batch.synchronizeDatabase;

import com.oskarro.batcher.batch.csvToDatabase.TrackProcessor;
import com.oskarro.batcher.batch.synchronizeDatabase.service.BackupDatabaseService;
import com.oskarro.batcher.batch.synchronizeDatabase.service.MainDatabaseService;
import com.oskarro.batcher.environment.main.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class DatabaseProcessor implements ItemProcessor<Track, Track> {

    private static final Logger log = LoggerFactory.getLogger(TrackProcessor.class);

    BackupDatabaseService backupDatabaseService;
    MainDatabaseService mainDatabaseService;

    public DatabaseProcessor(BackupDatabaseService backupDatabaseService, MainDatabaseService mainDatabaseService) {
        this.backupDatabaseService = backupDatabaseService;
        this.mainDatabaseService = mainDatabaseService;
    }

    @Override
    public Track process(final Track track) throws Exception {
        if (backupDatabaseService.validateSong(track.getCode()).equals("INSERT")) {
            System.out.println("[BACKUP] CREATING TRACK WITH CODE " + track.getCode());
            return track;
        } else {
            return null;
        }
    }
}
