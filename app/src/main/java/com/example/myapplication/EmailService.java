package com.example.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    private final Context context;
    private final String senderEmail;
    private final String senderPassword;

    public EmailService(Context context, String senderEmail, String senderPassword) {
        this.context = context;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
    }

    public void sendTicketEmail(String toEmail, String subject, String body, File attachment) {
        new SendEmailTask(toEmail, subject, body, attachment).execute();
    }

    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private final String toEmail;
        private final String subject;
        private final String body;
        private final File attachment;
        private String errorMessage;

        SendEmailTask(String toEmail, String subject, String body, File attachment) {
            this.toEmail = toEmail;
            this.subject = subject;
            this.body = body;
            this.attachment = attachment;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(senderEmail, senderPassword);
                            }
                        });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail, "SmartMarine"));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(toEmail));
                message.setSubject(subject);

                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(body);

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                if (attachment != null && attachment.exists()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    FileDataSource source = new FileDataSource(attachment);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(attachment.getName());
                    multipart.addBodyPart(attachmentPart);
                }

                message.setContent(multipart);

                Transport.send(message);
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(context,
                        "Ticket sent to " + toEmail,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context,
                        "Could not send email: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
