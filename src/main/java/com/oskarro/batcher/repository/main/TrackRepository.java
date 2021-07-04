package com.oskarro.batcher.repository.main;

import com.oskarro.batcher.model.main.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface TrackRepository extends JpaRepository<Track, Serializable> {

    long count();
}
