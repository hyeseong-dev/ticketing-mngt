package com.mgnt.ticketing.security;

import com.mgnt.ticketing.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private UserEntity userEntity;

    public UserDetailsImpl(){}

    public UserDetailsImpl(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public UserDetailsImpl(UserDetails userDetails) {
    }

    public UserEntity getUser() {
        return userEntity;
    }

    public void setEmailVerified(boolean emailVerified) {
        if (this.userEntity.getEmailVerified() != emailVerified) {
            this.userEntity.setEmailVerified(emailVerified);
        }
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userEntity.getRole().toString()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}