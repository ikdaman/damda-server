package com.damda.domain.mybook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordRes {
    @JsonIgnore
    private Long recordId;
    private String recordType;
    private String memo;
    private String recordDate;
}