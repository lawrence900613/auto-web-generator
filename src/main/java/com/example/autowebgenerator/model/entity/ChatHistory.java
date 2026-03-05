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
@Table(value = "chat_history")
public class ChatHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = "snowFlakeId")
    private Long id;

    private String message;

    /** "user" | "ai" */
    private String messageType;

    private Long appId;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    @Column(isLogicDelete = true)
    private Integer isDelete;
}
