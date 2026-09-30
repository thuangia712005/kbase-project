package com.kbase.kbase.dto;

import com.kbase.kbase.entity.Role;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    private String fullName;
    private Role role;
}
