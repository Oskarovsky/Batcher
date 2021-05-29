package com.oskarro.batcher.batch;

import com.oskarro.batcher.model.Track;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class TrackReader implements ItemReader<Track> {

    @Override
    public Track read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return null;
    }
}
