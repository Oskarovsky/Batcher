package com.oskarro.batcher.repository.backup;

import com.oskarro.batcher.model.backup.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface SongRepository extends JpaRepository<Song, Serializable> {

    long count();
}
