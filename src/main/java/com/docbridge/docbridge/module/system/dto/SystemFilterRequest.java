package com.docbridge.docbridge.module.system.dto;

import com.docbridge.docbridge.module.system.InteropSystemStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemFilterRequest {

    private String name;
    private InteropSystemStatus status;

    private int page = 0;
    private int size = 20;
}