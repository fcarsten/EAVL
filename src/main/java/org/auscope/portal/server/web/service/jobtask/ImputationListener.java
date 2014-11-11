package org.auscope.portal.server.web.service.jobtask;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.MailSender;

public class ImputationListener extends EmailListener {

    public ImputationListener(MailSender mailSender,
            VelocityEngine velocityEngine, String templateFilePath,
            String templateFileEncoding, String emailSender) {
        super(mailSender, velocityEngine, templateFilePath, templateFileEncoding,
                emailSender);

    }

}
