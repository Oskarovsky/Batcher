package com.oskarro.batcher.environment.backup.repo;

import com.oskarro.batcher.environment.backup.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Serializable> {

    long count();

    Song findByCode(String code);

    List<Song> findAllByCode(String code);
}
