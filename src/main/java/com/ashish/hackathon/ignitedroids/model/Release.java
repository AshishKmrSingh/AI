package com.ashish.hackathon.ignitedroids.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Release {

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("name")
    private String name;

    @JsonProperty("curr_stage")
    private String stage;

    @JsonProperty("release_date")
    private String releaseDate;
}
