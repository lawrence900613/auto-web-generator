package com.example.autowebgenerator.model.dto.app;

import com.example.autowebgenerator.common.PaginationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppQueryRequest extends PaginationRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String appName;

    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private Integer priority;

    private Long userId;
}
