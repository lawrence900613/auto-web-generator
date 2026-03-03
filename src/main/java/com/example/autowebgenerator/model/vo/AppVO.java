package com.example.autowebgenerator.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class AppVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String appName;

    private String cover;

    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private Date deployedTime;

    private Integer priority;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    /** Nested creator info. */
    private UserVO user;
}
