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

    private UserDetailsImpl(Long id, String email, String password, UserStatus status,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    private UserDetailsImpl(User user) {
        this(user.getId(), user.getEmail(), user.getPasswordHash(), user.getStatus(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    public static UserDetailsImpl from(User user) {
        return new UserDetailsImpl(user);
    }

    /**
     * Builds a principal from JWT claims (stateless, no DB lookup). The token is only
     * issued to authenticated users, so status is assumed ACTIVE; isEnabled() is not
     * consulted on the token-auth path (the filter sets an already-authenticated token).
     */
    public static UserDetailsImpl fromClaims(Long id, String email, String role) {
        return new UserDetailsImpl(id, email, null, UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
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
