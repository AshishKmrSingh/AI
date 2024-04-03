package com.ashish.hackathon.ignitedroids.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CLMCsvBean {
    private String Id;
    private String Item_Notes;
    private String Primary_Location;
    private String Summary;
    private String Plan_Parent_ID;
    private String Group;
    private String Work_Item_Type;
    private Double Effective_Estimate;
    private Double Progress_Completed_Story_Points;
    private Double Progress_Total_Story_Points;
    private Double Progress_Completed_Hours;
    private Double Progress_Total_Hours;
    private String Planned_For;
    private String Status;
    private String Owned_By;
    private String Filed_Against;
    private String Resolves;
    private String Implements_Requirement;
    private Double Estimate;
    private String Corrected_Estimate;
}
