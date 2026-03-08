package com.example.autowebgenerator.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(value = "app")
public class App implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = "snowFlakeId")
    private Long id;

    private String appName;

    private String cover;

    private String initPrompt;

    /** Code generation type: currently "vue_project" only (legacy modes disabled). */
    private String codeGenType;

    /** 6-char alphanumeric deploy identifier */
    private String deployKey;

    private Date deployedTime;

    /** 0 = normal, 99 = featured */
    private Integer priority;

    private Long userId;

    private Date editTime;

    private Date createTime;

    private Date updateTime;

    @Column(isLogicDelete = true)
    private Integer isDelete;
}
