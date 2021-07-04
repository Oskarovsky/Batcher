package com.oskarro.batcher.environment.main.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackRowMapper implements RowMapper<Track> {

    @Override
    public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Track.builder()
                .id(rs.getString("id"))
                .title(rs.getString("title"))
                .artist(rs.getString("artist"))
                .url(rs.getString("url"))
                .version(rs.getString("version"))
                .code(rs.getString("code"))
                .build();
    }

}
