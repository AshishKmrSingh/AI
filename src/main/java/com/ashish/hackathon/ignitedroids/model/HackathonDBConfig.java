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
public class HackathonDBConfig {

    @JsonProperty("url")
    private String dbUrl;

    @JsonProperty("user")
    private String user;

    @JsonProperty("password")
    private String password;

}
