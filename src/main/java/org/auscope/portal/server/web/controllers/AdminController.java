package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.security.oauth2.EAVLAuthority;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.security.oauth2.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller methods for various admin methods. This controller should
 * be protected so that administrators only can access it.
 * @author Josh Vote
 *
 */
@RequestMapping("admin")
@Controller
public class AdminController extends BasePortalController {

    private UserRepository userRepository;

    @Autowired
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Enumerates every user and their roles, returns the JSON encoding of these users.
     * @return
     */
    @RequestMapping("getUsers.do")
    public ModelAndView getUsers() {
        List<ModelMap> users = new ArrayList<ModelMap>();

        for (EavlUser user : userRepository.findAll()) {
            ModelMap mappedUser = new ModelMap();

            List<String> roles = new ArrayList<String>();
            for (GrantedAuthority auth : user.getAuthorities()) {
                roles.add(auth.getAuthority());
            }

            mappedUser.put("userName", user.getUsername());
            mappedUser.put("email", user.getEmail());
            mappedUser.put("fullName", user.getFullName());
            mappedUser.put("authorities", roles);

            users.add(mappedUser);
        }

        return generateJSONResponseMAV(true, users, "");
    }

    /**
     * Adds a role to a specified user. If the role already exists, this has no effect
     * @param userName
     * @param role
     * @return
     */
    @RequestMapping("addUserRole.do")
    public ModelAndView addUserRole(@RequestParam("userName") String userName, @RequestParam("role") String role) {
        EavlUser user = userRepository.findOne(userName);
        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        Set<EAVLAuthority> authorities = (Set<EAVLAuthority>) user.getAuthorities();
        EAVLAuthority auth = new EAVLAuthority(role);
        if (authorities.contains(auth)) {
            return generateJSONResponseMAV(true);
        }

        authorities.add(auth);
        userRepository.saveAndFlush(user);
        return generateJSONResponseMAV(true);
    }

    /**
     * Deletes a role from the specified user. If the role doesn't exist, this has no effect
     * @param userName
     * @param role
     * @return
     */
    @RequestMapping("deleteUserRole.do")
    public ModelAndView deleteUserRole(@RequestParam("userName") String userName, @RequestParam("role") String role) {
        EavlUser user = userRepository.findOne(userName);
        if (user == null) {
            return generateJSONResponseMAV(false);
        }


        Set<EAVLAuthority> authorities = (Set<EAVLAuthority>) user.getAuthorities();
        EAVLAuthority auth = new EAVLAuthority(role);
        if (!authorities.contains(auth)) {
            return generateJSONResponseMAV(true);
        }

        authorities.remove(auth);
        userRepository.saveAndFlush(user);
        return generateJSONResponseMAV(true);
    }
}
