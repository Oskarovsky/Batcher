package com.oskarro.batcher.batch.csvToDatabase;

import com.oskarro.batcher.model.main.Track;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;

import java.util.List;

public class TrackItemReader implements ItemReader<Track> {

    private final String content;

    private ItemReader<Track> delegate;

    public TrackItemReader(String content) {
        this.content = content;
    }

    @Override
    public Track read() throws Exception {
        if (delegate == null) {
            delegate = new IteratorItemReader<>(customers());
        }
        return delegate.read();
    }

    private List<Track> customers() {
        return null;
    }
}
