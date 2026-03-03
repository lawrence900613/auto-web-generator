package com.example.autowebgenerator.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** User can only update their own app's name. */
@Data
public class AppUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String appName;
}
