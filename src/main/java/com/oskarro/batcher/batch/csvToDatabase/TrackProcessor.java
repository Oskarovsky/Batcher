package com.oskarro.batcher.batch.csvToDatabase;

import com.oskarro.batcher.model.main.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;


public class TrackProcessor implements ItemProcessor<Track, Track> {

    private static final Logger log = LoggerFactory.getLogger(TrackProcessor.class);

    @Override
    public Track process(final Track track) {
        final String id = track.getId();
        final String title = track.getTitle();
        final String artist = track.getArtist();
        final String version = track.getVersion();
        final String url = track.getUrl();

        final Track transformedTrack = new Track(id, title, artist, version, url);

        log.info("Converting (" + track + ") into (" + transformedTrack + ")");

        return transformedTrack;
    }
}
