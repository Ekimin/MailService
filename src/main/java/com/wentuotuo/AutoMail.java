package com.wentuotuo;

import com.wentuotuo.mail.MailHandler;
import com.wentuotuo.mail.MailInfo;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

public class AutoMail {

    public static void sendHtmlMail(MailInfo mailInfo) throws MessagingException, UnsupportedEncodingException {
        Message message = getMessage(mailInfo);
        // MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
        Multipart mainPart = new MimeMultipart();
        // 创建一个包含HTML内容的MimeBodyPart
        BodyPart html = new MimeBodyPart();

        // 设置HTML内容
        html.setContent(mailInfo.getContent(), "text/html; charset=utf-8");
        mainPart.addBodyPart(html);
        // 将MiniMultipart对象设置为邮件内容
        message.setContent(mainPart);
        Transport.send(message);
    }

    private static Message getMessage(MailInfo mailInfo) throws MessagingException, UnsupportedEncodingException {
        final Properties p = new Properties();
        p.setProperty("mail.smtp.host", mailInfo.getHost());
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.smtp.user", mailInfo.getFormName());
        p.setProperty("mail.smtp.pass", mailInfo.getFormPassword());

        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session session = Session.getInstance(p, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(p.getProperty("mail.smtp.user"), p.getProperty("mail.smtp.pass"));
            }
        });

        session.setDebug(true);
        Message message = new MimeMessage(session);
        //消息发送的主题
        message.setSubject(mailInfo.getSubject());
        //接受消息的人
        message.setReplyTo(InternetAddress.parse(mailInfo.getReplayAddress()));
        //消息的发送者
        message.setFrom(new InternetAddress(p.getProperty("mail.smtp.user"), "Steam Support"));
        // 创建邮件的接收者地址，并设置到邮件消息中
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailInfo.getToAddress()));
        // 消息发送的时间
        message.setSentDate(new Date());

        return message;
    }

    /**
     * 随机取一个发信邮箱
     *
     * @param mailInfo
     * @return
     */
    public static MailInfo getMailInfo(MailInfo mailInfo) {
        String str = "georgedeps2@163.com";
        mailInfo.setHost("smtp.163.com");
        mailInfo.setFormName(str);
        mailInfo.setFormPassword("george2009");//授权码
        mailInfo.setReplayAddress("noreply@steampowered.com");


        return mailInfo;
    }

    public static void main(String[] args) {
        String mail = "10216908@qq.com"; //发送对象的邮箱
        String title = "你的邮件10216908申请";
        String content = "<div>你不在学校吗？</div><br/><hr/><div>记得28号3来学校</div>";
        MailInfo mailInfo = new MailInfo();
        getMailInfo(mailInfo);

        mailInfo.setToAddress(mail);
        mailInfo.setSubject(title);
        mailInfo.setContent(content);
        try {
            //MailSendUtil.sendTextMail(info);
            AutoMail.sendHtmlMail(mailInfo);
        } catch (Exception e) {
            System.out.print("'" + title + "'的邮件发送失败！");
            e.printStackTrace();
        }
    }
}
