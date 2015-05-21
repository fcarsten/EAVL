package org.auscope.portal.server.security.oauth2;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.security.core.GrantedAuthority;

/**
 * Similar to the SimpleGrantedAuthority but annotated with JPA tags for persistance
 *
 * @author Josh Vote
 * @see org.springframework.security.core.authority.SimpleGrantedAuthority
 *
 */
@Entity
public class EAVLAuthority implements GrantedAuthority {
    private static final long serialVersionUID = 151022306510042949L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String role;

    public EAVLAuthority() {
        //JPA
    }

    public EAVLAuthority(String role) {
        this.role = role;
    }

    public String getAuthority() {
        return role;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof EAVLAuthority) {
            return role.equals(((EAVLAuthority) obj).role);
        }

        if (obj instanceof String) {
            return role.equals(obj);
        }

        return false;
    }

    public int hashCode() {
        return this.role.hashCode();
    }

    public String toString() {
        return this.role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


}
