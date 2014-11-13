package org.auscope.portal.server.web.service.jobtask;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * A listener that sends an email upon job completion (if JobTask.notificationEmail is set)
 * @author Josh Vote
 *
 */
public class EmailListener implements JobTaskListener {

    protected final Log log = LogFactory.getLog(getClass());

    private MailSender mailSender;
    private VelocityEngine velocityEngine;
    private String templateFilePath;
    private String templateFileEncoding;
    private String emailSender;

    public EmailListener(MailSender mailSender, VelocityEngine velocityEngine, String templateFilePath, String templateFileEncoding, String emailSender) {
        super();
        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;
        this.templateFilePath = templateFilePath;
        this.templateFileEncoding = templateFileEncoding;
        this.emailSender = emailSender;
    }

    protected void sendNotificationEmail(String email, String id, JobTask task, boolean isErrorMail) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", task.getJob().getName());
        String content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateFilePath, templateFileEncoding, model);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(this.emailSender);
        msg.setTo(email);
        if (isErrorMail) {
            msg.setSubject(String.format("EAVL task %1$s has errored", task.getJob().getName()));
        } else {
            msg.setSubject(String.format("EAVL task %1$s has finished", task.getJob().getName()));
        }
        msg.setText(content);

        try {
            synchronized(this.mailSender) {
                this.mailSender.send(msg);
            }
        } catch (Exception ex) {
            log.error("Sending of email notification failed for job id [" + task.getJob().getId() + "].", ex);
        }
    }

    @Override
    public void handleTaskFinish(String id, JobTask task) {
        boolean isError = false;
        try {
            task.get(); //Force any exceptions to be rethrown
        } catch (Exception ex) {
            log.error(String.format("JobTask %1$s threw an exception during computation. Sending error email instead", task));
            isError = true;
        }


        if (task.getEmail() != null) {
            this.sendNotificationEmail(task.getEmail(), id, task, isError);
        }
    }

}
