package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.Charsets;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.security.oauth2.EavlUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller methods for the feedback widget
 * @author Josh Vote
 *
 */
@RequestMapping("eavl/feedback")
@Controller
public class FeedbackController extends BasePortalController {

    private JavaMailSender mailSender;
    @Value("${env.feedback.email}")
    private String emailSenderAddress;

    public String getEmailSenderAddress() {
		return emailSenderAddress;
	}

	public void setEmailSenderAddress(String emailSenderAddress) {
		this.emailSenderAddress = emailSenderAddress;
	}

	@Autowired
    public FeedbackController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RequestMapping("/sendFeedback.do")
    public void sendFeedback(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal EavlUser user,
            @RequestParam("issue") String issue,
            @RequestParam("email") boolean email,
            @RequestParam("screenshot") String screenshotString,
            @RequestParam("metadata") String metadataString) {

        try {
            if (user == null) {
                log.warn("Unauthorized feedback request.");
                response.sendError(HttpStatus.SC_UNAUTHORIZED);
                return;
            }


            JSONObject metadata = JSONObject.fromObject(metadataString);

            Matcher matcher = Pattern.compile("data:([^;]*);base64,(.*)").matcher(screenshotString);
            if (!matcher.matches()) {
                log.error("Malformed feedback screenshot: " + screenshotString);
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            StringBuilder bodyText = new StringBuilder();
            bodyText.append("<html><body><p><b>Issue:</b> ");
            bodyText.append(issue);
            bodyText.append("</p>");

            bodyText.append("<p><b>Session Job ID:</b> ");
            bodyText.append(request.getParameter("sessionJobId"));
            bodyText.append("</p>");

            bodyText.append("<p><b>User:</b> ");
            bodyText.append(user.getFullName());
            bodyText.append("</p>");

            bodyText.append("<p><b>Email:</b> ");
            bodyText.append(user.getEmail());
            bodyText.append("</p>");

            bodyText.append("<p><b>Contact Me:</b> ");
            bodyText.append(email ? "Yes" : "No");
            bodyText.append("</p>");

            for (Object key : metadata.keySet()) {
                bodyText.append("<p><b>");
                bodyText.append(key.toString());
                bodyText.append(":</b> ");
                bodyText.append(metadata.get(key).toString());
                bodyText.append("</p>");
            }

            bodyText.append("<img src='cid:screenshot'></body></html>");
            String imageContentType = matcher.group(1);
            String imageBase64Data = matcher.group(2);

            byte[] imageBytes = Base64.decodeBase64(imageBase64Data.getBytes(Charsets.UTF_8));

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);

            helper.setFrom(emailSenderAddress);
            helper.setTo(emailSenderAddress);
            if (email) {
                helper.setCc(user.getEmail());
            }
            helper.setSubject("EAVL Issue");
            helper.setText(bodyText.toString(), true);
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
