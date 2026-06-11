package com.sba301.backend.dto.mapper;

import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.dto.request.RegisterRequest;
import com.sba301.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "role", expression = "java(defaultRole())")
    @Mapping(target = "status", expression = "java(defaultStatus())")
    User toEntity(RegisterRequest request);

    default UserRole defaultRole() {
        return UserRole.CUSTOMER;
    }

    default UserStatus defaultStatus() {
        return UserStatus.ACTIVE;
    }
}
