package com.ouyang.community.entity;

import com.ouyang.community.utils.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends EntityBase implements UserDetails {
    private String username;
    private String password;
    private String salt;
    private String email;

    /**
     * 0-普通用户; 1-超级管理员; 2-版主;
     */
    private Integer type;

    /**
     * 0-未激活; 1-已激活;
     */
    private Integer status;
    private String activationCode;
    private String headerUrl;

    /**
     * Security根据这个获取用户权限，实现方式为根据type获取用户权限
     *
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<GrantedAuthority>() {{
            add(() -> {
                switch (type) {
                    case 1:
                        return Constant.AUTHORITY_ADMIN;
                    case 2:
                        return Constant.AUTHORITY_MODERATOR;
                    default:
                        return Constant.AUTHORITY_USER;
                }
            });
        }};
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return status == 1;
    }
}
