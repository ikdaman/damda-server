package com.damda.domain.mybook.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordRes {
    private String recordType;
    private String memo;
    private String recordDate;
}