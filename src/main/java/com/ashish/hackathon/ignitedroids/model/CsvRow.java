package com.ashish.hackathon.ignitedroids.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CsvRow {
    private String release;
    private long devBugs;
    private long qualityBugs;
    private long designBugs;
    private long prodBugs;
    private long testBugs;
    private long filesChanged;
    private long addedLines;
    private long deletedLines;
}
