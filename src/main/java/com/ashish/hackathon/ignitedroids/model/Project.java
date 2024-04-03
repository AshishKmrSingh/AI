package com.ashish.hackathon.ignitedroids.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Project {

    @JsonProperty("bitbucket_link")
    private String bitBucketLink;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("tag_prefix")
    private String tag_prefix;

    @JsonProperty("work_dir")
    private String workDir;

    @JsonProperty("result_file_path")
    private String resultFilePath;

    @JsonProperty("releases")
    private Releases releases;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class Releases {

        @JsonProperty("released")
        private List<String> released;

        @JsonProperty("planned")
        private List<Release> planned;
    }
}
