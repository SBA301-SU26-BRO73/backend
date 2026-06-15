package com.sba301.backend.config.security;

import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.status = user.getStatus();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public static UserDetailsImpl from(User user) {
        return new UserDetailsImpl(user);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
