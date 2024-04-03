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
public class ProjectConfig {

    @JsonProperty("project")
    private Project project;

    @JsonProperty("database")
    private HackathonDBConfig database;
}
