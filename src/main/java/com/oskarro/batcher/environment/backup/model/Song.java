package com.oskarro.batcher.environment.backup.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "songs")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private String id;
    private String title;
    private String artist;
    private String version;
    private String url;
    private String code;
}
