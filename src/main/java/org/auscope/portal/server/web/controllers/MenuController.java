package org.auscope.portal.server.web.controllers;

import java.awt.Menu;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.eavl.EAVLJob;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.auscope.portal.server.web.service.EAVLJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller that handles all {@link Menu}-related requests,
 *
 * @author Jarek Sanders
 * @author Josh Vote
 */
@Controller
public class MenuController {

   protected final Log logger = LogFactory.getLog(getClass());

   private EAVLJobService jobService;

   @Autowired
   public MenuController(EAVLJobService jobService) {
       this.jobService = jobService;
   }

   /**
    * Adds the google maps/analytics keys to the specified model
    * @param mav
    */
   private void addGoogleKeys(ModelAndView mav) {
//       String googleKey = hostConfigurer.resolvePlaceholder("HOST.googlemap.key");
//       String analyticKey = hostConfigurer.resolvePlaceholder("HOST.google.analytics.key");
//
//       mav.addObject("googleKey", googleKey);
//       if (analyticKey != null && !analyticKey.isEmpty()) {
//           mav.addObject("analyticKey", analyticKey);
//       }
   }

   /**
    * Adds a number of manifest specific variables to the model
    * @param mav
    * @param request
    */
   private void addManifest(ModelAndView mav, HttpServletRequest request) {
       String appServerHome = request.getSession().getServletContext().getRealPath("/");
       File manifestFile = new File(appServerHome,"META-INF/MANIFEST.MF");
       Manifest mf = new Manifest();
       try {
          mf.read(new FileInputStream(manifestFile));
          Attributes atts = mf.getMainAttributes();
          if (mf != null) {
             mav.addObject("specificationTitle", atts.getValue("Specification-Title"));
             mav.addObject("implementationVersion", atts.getValue("Implementation-Version"));
             mav.addObject("implementationBuild", atts.getValue("Implementation-Build"));
             mav.addObject("buildDate", atts.getValue("buildDate"));
             mav.addObject("buildJdk", atts.getValue("Build-Jdk"));
             mav.addObject("javaVendor", atts.getValue("javaVendor"));
             mav.addObject("builtBy", atts.getValue("Built-By"));
             mav.addObject("osName", atts.getValue("osName"));
             mav.addObject("osVersion", atts.getValue("osVersion"));

             mav.addObject("serverName", request.getServerName());
             mav.addObject("serverInfo", request.getSession().getServletContext().getServerInfo());
             mav.addObject("serverJavaVersion", System.getProperty("java.version"));
             mav.addObject("serverJavaVendor", System.getProperty("java.vendor"));
             mav.addObject("javaHome", System.getProperty("java.home"));
             mav.addObject("serverOsArch", System.getProperty("os.arch"));
             mav.addObject("serverOsName", System.getProperty("os.name"));
             mav.addObject("serverOsVersion", System.getProperty("os.version"));
          }
       } catch (IOException e) {
           /* ignore, since we'll just leave an empty form */
           logger.info("Error accessing manifest: " + e.getMessage());
           logger.debug("Exception:", e);
       }
   }

   /**
    * Handles all HTML page requests by mapping them to an appropriate view (and adding other details).
    * @param request
    * @param response
    * @return
    * @throws IOException
    */
   @RequestMapping("/{name}.html")
   public ModelAndView handleHtmlToView(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal EavlUser user,
           @PathVariable String name) throws IOException {
       return handleHtmlToView(request, response, user, null, name);
   }

   /**
    * Handles all HTML page requests by mapping them to an appropriate view (and adding other details).
    * @param request
    * @param response
    * @return
    * @throws IOException
    */
   @RequestMapping("/{directory}/{name}.html")
   public ModelAndView handleHtmlToView(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal EavlUser user,
           @PathVariable String directory, @PathVariable String name) throws IOException {
       //Detect whether this is a new session or not...
       HttpSession session = request.getSession();
       boolean isNewSession = session.getAttribute("existingSession") == null;
       session.setAttribute("existingSession", true);

       //Decode our request to get the view name we are actually requesting
       if (name == null || name.isEmpty()) {
           response.sendError(HttpStatus.SC_NOT_FOUND, "Resource not found");
           return null;
       }
       String resourceName = (directory == null || directory.isEmpty()) ? name : directory + "/" + name ;

       //If the sessionJobId parameter is set, let's update the session job transparently
       String sessionJobId = request.getParameter("sessionJobId");
       if (sessionJobId != null) {
           try {
               EAVLJob job = jobService.getUserJobById(request, user, Integer.parseInt(sessionJobId));
               if (job == null) {
                   throw new PortalServiceException(String.format("Job '%1$s' DNE or current user doesnt have permission to access it.", sessionJobId));
               }
               jobService.setSessionJob(job, request, user);
           } catch (Exception ex) {
               //I dont think there is much we can do here. Its either
               //a malicious user or a bug with our client code.
               logger.error("Unable to update session job: ", ex);
               response.sendError(HttpStatus.SC_FORBIDDEN);
               return null;
           }
       }

       //Give the user the view they are actually requesting
       ModelAndView mav = new ModelAndView(resourceName);

       //We don't just want "email@domain" in the page. Let's obscure it slightly so it cant be
       //scraped easily
//       String[] obscuredEmailParts = hostConfigurer.resolvePlaceholder("env.feedback.email").split("@");
//       String obscuredEmail = "\"" + obscuredEmailParts[0] + "\" + \"@\" + \"" + obscuredEmailParts[1] + "\"";

       mav.addObject("isNewSession", isNewSession);
//       mav.addObject("OBSCURED_CONTACT_EMAIL", obscuredEmail);
       mav.addObject("CURRENT_USER_EMAIL", (user == null) ? "null" : ("'" + user.getEmail() + "'"));


       //Customise the model as required
       addGoogleKeys(mav); //always add the google keys
       if (resourceName.equals("about") || resourceName.equals("admin")) {
           addManifest(mav, request); //The manifest details aren't really required by much
       }



       return mav;
   }
}
