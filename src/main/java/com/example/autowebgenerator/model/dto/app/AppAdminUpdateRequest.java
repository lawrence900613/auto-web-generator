package com.example.autowebgenerator.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Admin can update name, cover, and priority (setting 99 = featured). */
@Data
public class AppAdminUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String appName;

    private String cover;

    private Integer priority;
}
