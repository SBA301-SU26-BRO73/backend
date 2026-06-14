package com.sba301.backend.dto.mapper;

import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.dto.request.CourtOwnerRegisterRequest;
import com.sba301.backend.dto.request.CustomerRegisterRequest;
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
    @Mapping(target = "role", expression = "java(customerRole())")
    @Mapping(target = "status", expression = "java(activeStatus())")
    User toCustomerEntity(CustomerRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "role", expression = "java(adminRole())")
    @Mapping(target = "status", expression = "java(pendingStatus())")
    User toCourtOwnerEntity(CourtOwnerRegisterRequest request);

    default UserRole customerRole() { return UserRole.CUSTOMER; }
    default UserRole adminRole() { return UserRole.ADMIN; }
    default UserStatus activeStatus() { return UserStatus.ACTIVE; }
    default UserStatus pendingStatus() { return UserStatus.PENDING_APPROVAL; }
}
