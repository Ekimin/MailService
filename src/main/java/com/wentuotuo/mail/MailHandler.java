package com.wentuotuo.mail;

import com.wentuotuo.wtt.WTT;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

public class MailHandler {

    private static Properties smtpProps = new Properties();
    private static String emailAccount = "";
    private static String emailPassword = "";
    private static boolean isInit = false;

    public static boolean init() {

        if (!isInit) {

            emailAccount = WTT.getProperty("EMAIL_ACCOUNT", "10216908@qq.com");
            emailPassword = DESTools
                    .Decrypt(WTT.getProperty("EMAIL_PASSWORD", "72O27H8LT38N3EG74J18J3ANU44K3N44PS73OU11HYFMV32LR29"));

            smtpProps.setProperty("mail.transport.protocol", "smtp");
            //smtpProps.setProperty("mail.smtp.host", WTT.getProperty("EMAIL_HOST", "smtp.amarsoft.com"));
            smtpProps.setProperty("mail.smtp.auth", "true");

            isInit = true;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {

        MailHandler.init();
        MailHandler.sendMail("2282238122@qq.com",
                "大神吃鸡  ·额吧", "你就是大神吗？酷啊", false);

    }

    /**
     * 连接邮件服务器
     *
     * @param transport
     * @param model     1：使用内网；2：使用外网,默认内网
     * @return
     */
    public static boolean connect(Transport transport, int model) {
        if (model == 2) {
            smtpProps.setProperty("mail.smtp.host", WTT.getProperty("EMAIL_HOST", "smtp.qq.com"));
            //SSL验证
            final String smtpPort = "465";
            smtpProps.setProperty("mail.smtp.port", smtpPort);
            smtpProps.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            smtpProps.setProperty("mail.smtp.socketFactory.fallback", "false");
            smtpProps.setProperty("mail.smtp.socketFactory.port", smtpPort);
        } else {
            smtpProps.setProperty("mail.smtp.host", WTT.getProperty("EMAIL_HOST", "192.168.1.2"));
        }
        try {
            transport.connect(emailAccount, emailPassword);
            return true;
        } catch (Exception e) {
            WTT.getLog().error(e);
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendMail(String recipient, String subject, String content, boolean isHtml) {

        try {
            Session session = Session.getDefaultInstance(smtpProps);
//			session.setDebug(true);
            MimeMessage message = createTxtMessage(session, emailAccount, recipient, subject, content, isHtml);

            Transport transport = session.getTransport();

            if (!connect(transport, 2)) {
                WTT.getLog().error("尝试外网连接失败！请联系邮件服务器管理员");
            }

            transport.sendMessage(message, message.getAllRecipients());

            transport.close();
            return true;

        } catch (NoSuchProviderException e) {
            WTT.getLog().error("provider is not found:" + e.toString(), e);
            return false;
        } catch (AuthenticationFailedException e) {
            WTT.getLog().error("authentication failed:" + e.toString(), e);
            return false;
        } catch (MessagingException e) {
            WTT.getLog().error("message error:" + e.toString(), e);
            return false;
        } catch (IllegalStateException e) {
            WTT.getLog().error("service is already connected:" + e.toString(), e);
            return false;
        } catch (UnsupportedEncodingException e) {
            WTT.getLog().error("UnsupportedEncodingException:" + e.toString(), e);
            return false;
        } catch (Exception e) {
            WTT.getLog().error("发邮件失败:" + e.toString(), e);
            return false;
        }

    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session   和服务器交互的会话
     * @param from      邮件发送人
     * @param recipient 邮件接收人,多人用英文逗号","分隔，逗号前后不能有空格
     * @param subject   邮件主题
     * @param content   邮件内容
     * @return MimeMessage
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */

    public static MimeMessage createTxtMessage(Session session, String from, String recipient, String subject,
                                               String content, boolean isHtml) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(from, "userName", "UTF-8"));

        if (recipient.contains(",")) {
            String[] recipientList = recipient.split(",");

            for (int i = 0; i < recipientList.length; i++) {
                checkSetRecipient(message, recipientList[i]);
            }
        } else {

            checkSetRecipient(message, recipient);
        }

        message.setSubject(subject, "UTF-8");

        if (isHtml) {
            message.setContent(content, "text/html;charset=UTF-8");
        } else {
            message.setContent(content, "text/plain;charset=UTF-8");
        }

        message.setSentDate(new Date());

        message.saveChanges();

        return message;
    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param message 消息
     * @param mail    邮件地址
     * @return boolean
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    private static boolean checkSetRecipient(MimeMessage message, String mail)
            throws UnsupportedEncodingException, MessagingException {

        if (null == message || null == mail || mail.equals("")) {
            WTT.getLog().error("非法参数");
            return false;
        }

        String tempMail = mail;
        String recipientType = "TO";
        if (tempMail.contains(":")) {

            String rType = tempMail.substring(0, tempMail.indexOf(':')).toUpperCase();
            if (rType.equals("TO") || rType.equals("CC") || rType.equals("BCC")) {
                recipientType = rType;
            } else {
                WTT.getLog().error("非法邮件地址格式:" + mail);
                return false;
            }

            tempMail = tempMail.substring(tempMail.indexOf(':') + 1);
        }

        System.out.println("tempMail=" + tempMail + ",recipientType=" + recipientType);
        String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

        if (!tempMail.matches(EMAIL_REGEX)) {
            WTT.getLog().error("非法邮件地址格式:" + tempMail);
            return false;
        } else {

            if (recipientType.equals("TO")) {
                message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(tempMail, "", "UTF-8"));
//				message.addHeader("To", tempMail);

            } else if (recipientType.equals("CC")) {
                message.addRecipient(MimeMessage.RecipientType.CC, new InternetAddress(tempMail, "", "UTF-8"));
//				message.addHeader("Cc", tempMail);

            } else if (recipientType.equals("BCC")) {
                message.addRecipient(MimeMessage.RecipientType.BCC, new InternetAddress(tempMail, "", "UTF-8"));
//				message.addHeader("Bcc", tempMail);

            } else {
            }

            return true;
        }

    }

}
