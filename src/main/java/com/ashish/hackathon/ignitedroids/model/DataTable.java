package com.ashish.hackathon.ignitedroids.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DataTable {
    private String past_release;
    private long filesChanged;
    private long linesAdded;
    private long linesDeleted;
    private String stage;
    private int bugs;
}
