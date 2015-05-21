package org.auscope.portal.server.security.oauth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.racquettrack.security.oauth.OAuth2UserDetailsLoader;

/**
 * A class for loading user details from google OAuth2 authentication and matching them
 * to an internal database of roles.
 *
 * Additional roles can be configured for select users based on user ID
 *
 * @author Josh Vote
 *
 */
public class GoogleOAuth2UserDetailsLoader implements
        OAuth2UserDetailsLoader<EavlUser> {

    protected String defaultRole;
    protected Map<String, List<EAVLAuthority>> rolesByEmail;

    @Autowired
    private UserRepository userRepository;
    /**
     * Creates a new GoogleOAuth2UserDetailsLoader that will assign defaultRole to every user
     * as a granted authority.
     * @param defaultRole
     */
    public GoogleOAuth2UserDetailsLoader(String defaultRole) {
        this(defaultRole, null);
    }

    /**
     * Creates a new GoogleOAuth2UserDetailsLoader that will assign defaultRole to every user
     * AND any authorities found in rolesByUser if the ID matches the current user ID
     * @param defaultRole
     * @param rolesByUser
     */
    public GoogleOAuth2UserDetailsLoader(String defaultRole, Map<String, List<String>> rolesByEmail) {
        this.defaultRole = defaultRole;
        this.rolesByEmail = new HashMap<String, List<EAVLAuthority>>();

        if (rolesByEmail != null) {
            for (Entry<String, List<String>> entry : rolesByEmail.entrySet()) {
                List<String> authorityStrings = entry.getValue();
                List<EAVLAuthority> authorities = new ArrayList<EAVLAuthority>(authorityStrings.size());
                for (String authority : authorityStrings) {
                    authorities.add(new EAVLAuthority(authority));
                }

                this.rolesByEmail.put(entry.getKey(), authorities);
            }
        }
    }

    /**
     * Always returns null - users will always need to be created
     */
    @Override
    public EavlUser getUserByUserId(String id) {
        EavlUser res = userRepository.findOne(id);
        
        //Always ensure that the default role is set
        if (res != null) {
            if (!res.getAuthorities().contains(defaultRole)) {
                Collection<EAVLAuthority> authorities = (Collection<EAVLAuthority>) res.getAuthorities();
                authorities.add(new EAVLAuthority(defaultRole));
            }
        }
        
        return res;
    }

    @Override
    public boolean isCreatable(Map<String, Object> userInfo) {
        return userInfo.containsKey("id");
    }

    /**
     * Extracts keys from userInfo and applies them to appropriate properties in user
     * @param user
     * @param userInfo
     */
    protected void applyInfoToUser(EavlUser user,  Map<String, Object> userInfo) {
        user.setEmail(userInfo.get("email").toString());
        user.setFullName(userInfo.get("name").toString());
    }

    @Override
    public UserDetails createUser(String id, Map<String, Object> userInfo) {
        EavlUser newUser = new EavlUser(id);
        applyInfoToUser(newUser, userInfo);

        //Apply default authorities
        HashSet<EAVLAuthority> authorities = new HashSet<EAVLAuthority>();
        authorities.add(new EAVLAuthority(defaultRole));

        if (rolesByEmail != null) {
            List<EAVLAuthority> additionalAuthorities = rolesByEmail.get(newUser.getEmail());
            if (additionalAuthorities != null) {
                authorities.addAll(additionalAuthorities);
            }
        }
        newUser.setAuthorities(authorities);

        userRepository.saveAndFlush(newUser);
        return newUser;
    }


    @Override
    public UserDetails updateUser(UserDetails userDetails,
            Map<String, Object> userInfo) {

        if (userDetails instanceof EavlUser) {
            applyInfoToUser((EavlUser) userDetails, userInfo);
            userRepository.saveAndFlush((EavlUser) userDetails);
        }

        return userDetails;
    }

}
