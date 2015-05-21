package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.security.oauth2.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
