package com.oskarro.batcher.repository;

import com.oskarro.batcher.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface TrackRepository extends JpaRepository<Track, Serializable> {

    long count();
}
