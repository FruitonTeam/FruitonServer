package cz.cuni.mff.fruiton.dao;

import cz.cuni.mff.fruiton.component.util.UserInfoCache;
import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class UserIdHolder implements Principal, UserDetails {

    private static final String ROLE_USER = "ROLE_USER";

    private final String id;

    private final String username;

    private final String password;

    private UserIdHolder(final String id, final String username, final String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public static UserIdHolder of(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot create id holder for null user");
        }
        return new UserIdHolder(user.getId(), user.getLogin(), user.getPassword());
    }

    public String getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public String getUsername() {
        return username;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getGrantedAuthorities(getRoles());
    }

    private List<GrantedAuthority> getGrantedAuthorities(final Collection<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private List<String> getRoles() {
        return List.of(ROLE_USER);
    }

    public UserInfoCache.UserInfo getInfo() {
        return UserInfoCache.get(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserIdHolder that = (UserIdHolder) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    public boolean represents(final User user) {
        return id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserIdHolder{"
                + "id='" + id + '\''
                + ", username='" + username + '\''
                + '}';
    }
}
