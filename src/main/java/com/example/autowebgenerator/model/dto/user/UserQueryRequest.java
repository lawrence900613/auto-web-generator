package com.example.autowebgenerator.model.dto.user;

import com.example.autowebgenerator.common.PaginationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * Admin paginated user query request.
 * Inherits page/size/sortBy/sortOrder from PaginationRequest.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PaginationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String userAccount;

    private String userName;

    private String userRole;
}
