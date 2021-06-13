package com.oskarro.batcher.batch.csvToDatabase;

import javax.batch.api.chunk.ItemWriter;
import java.io.Serializable;
import java.util.List;

public class TrackWriter implements ItemWriter {

    @Override
    public void open(Serializable serializable) throws Exception {

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void writeItems(List<Object> list) throws Exception {

    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }

}
