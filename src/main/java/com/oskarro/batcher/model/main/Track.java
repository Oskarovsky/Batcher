package com.oskarro.batcher.model.main;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "tracks")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Track {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private String id;
    private String title;
    private String artist;
    private String version;
    private String url;


}
