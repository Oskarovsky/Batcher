package com.oskarro.batcher.environment.main.repo;

import com.oskarro.batcher.environment.main.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface TrackRepository extends JpaRepository<Track, Serializable> {

    long count();
}
