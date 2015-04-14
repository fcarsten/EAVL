package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.Charsets;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sun.istack.ByteArrayDataSource;

/**
 * Controller methods for the feedback widget
 * @author Josh Vote
 *
 */
@RequestMapping("feedback")
@Controller
public class FeedbackController extends BasePortalController {

    public static String CONTACT_EMAIL = "Josh" + ".Vote" + "@" + "csiro" + ".au"; //Just so the email isn't easily scrapable off github

    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private String templateFilePath;
    private String templateFileEncoding;

    @Autowired
    public FeedbackController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RequestMapping("/sendFeedback.do")
    public void sendFeedback(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal EavlUser user,
            @RequestParam("data") String dataString) {

        try {
            // BACON - don't commit this commented out
            /*if (user == null) {
                log.warn("Unauthorized feedback request.");
                response.sendError(HttpStatus.SC_UNAUTHORIZED);
                return;
            }*/
            //BACON - this is test code
            user = new EavlUser("BACON-fake-id");
            user.setEmail("jjv" + "ote" + "@" + "gm" + "ail." + "c" + "om"); //security through obscurity!

            JSONArray data = JSONArray.fromObject(dataString);

            String issueText = data.getJSONObject(0).getString("Issue");
            Matcher matcher = Pattern.compile("data:([^;]*);base64,(.*)").matcher(data.getString(1));
            if (!matcher.matches()) {
                log.error("Malformed feedback data string: " + dataString);
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            String imageContentType = matcher.group(1);
            String imageBase64Data = matcher.group(2);

            byte[] imageBytes = Base64.decodeBase64(imageBase64Data.getBytes(Charsets.UTF_8));

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);

            helper.setFrom(CONTACT_EMAIL);
            helper.setTo(CONTACT_EMAIL);
            helper.setCc(user.getEmail());
            helper.setSubject("EAVL Issue");
            helper.setText("<html><body><p>" + issueText + "</p><img src='cid:screenshot'></body></html>", true);
            helper.addInline("screenshot", new ByteArrayDataSource(imageBytes, imageContentType));

            mailSender.send(msg);
        } catch (Exception ex) {
            try {
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e) {
                log.error("Error sending error status code: ", e);
            }
            log.error("Error sending feedback: ", ex);
        }
    }
}
