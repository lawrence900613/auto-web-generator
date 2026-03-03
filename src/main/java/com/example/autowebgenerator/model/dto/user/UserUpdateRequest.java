package com.example.autowebgenerator.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Admin request to update an existing user.
 */
@Data
public class UserUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String userName;

    private String userAvatar;

    private String userProfile;

    /** Role: "user" or "admin". */
    private String userRole;
}
