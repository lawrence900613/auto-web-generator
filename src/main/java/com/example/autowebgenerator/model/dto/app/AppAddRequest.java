package com.example.autowebgenerator.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Initial prompt — the only field required from the user. */
    private String initPrompt;
}
