package org.igov.io.mail;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.igov.io.GeneralConfig;
import org.igov.io.mail.unisender.CreateCampaignRequest;
import org.igov.io.mail.unisender.CreateEmailMessageRequest;
import org.igov.io.mail.unisender.UniResponse;
import org.igov.io.mail.unisender.UniSender;
import org.igov.service.business.msg.MsgService;
import org.igov.util.MethodsCallRunnerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * @author Belyavtsev Vladimir Vladimirovich (BW)
 */
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Service("mail")
@Scope("prototype")
public class Mail extends Abstract_Mail {

    @Autowired
    MethodsCallRunnerUtil methodCallRunner;

    @Autowired
    GeneralConfig generalConfig;

    @Autowired
    UniSender oUniSender;

    @Autowired
    MsgService msgService;

    private final static Logger LOG = LoggerFactory.getLogger(Mail.class);
    private static final Logger LOG_BIG = LoggerFactory.getLogger("MailBig");

    Properties oProps = new Properties();
    String DEFAULT_ENCODING = "UTF-8";
    private Session oSession = null;
    //private String sContext="";
    private Multipart oMultiparts = new MimeMultipart();

    public Mail() {
    }

    @Override
    public void send() throws EmailException {
        LOG.info("(getFrom()={})", getFrom());
        LOG.info("(getTo()={})", getTo());
        String sTo = getTo();
        String sToNew = sTo;
//        sToNew = sToNew.replace("\\<","<");
//        sToNew = sToNew.replace("\\>",">");
        sToNew = sToNew.replace("\"", "");
        sToNew = sToNew.replace("\"", "");
        //sTo=sTo.replaceAll("\"", "");
        if (!sToNew.equals(sTo)) {
            LOG.info("(getTo()(fixed)={})", sToNew);
            _To(sToNew);
        }
        LOG.info("(getHead()={})", getHead());

        Boolean bUniSender = generalConfig.isEnable_UniSender_Mail();
        LOG.info("(bUniSender={})", bUniSender);
        //LOG.debug("(getFrom()={})", getFrom());
        //LOG.debug("(getTo()={})", getTo());
        //LOG.debug("(getHead()={})", getHead());
        //LOG.debug("(getBody={})", getBody());
        LOG_BIG.info("(bUniSender={})", bUniSender);
        LOG_BIG.debug("(getFrom()={})", getFrom());
        LOG_BIG.debug("(getTo()={})", getTo());
        LOG_BIG.debug("(getHead()={})", getHead());
        LOG_BIG.debug("(getBody={})", getBody());

        /*
         if(getTo().contains("Lyud84@mail.ru")
         || getTo().contains("vira.haman@ideabank.ua")
         || getTo().contains("akostyuk@winnerauto.ua")
         || getTo().contains("nikolay.mazur@gmail.com")
         || getTo().contains("advokat.rovenskiy@gmail.com")
         || getTo().contains("vika2010@ua.fm")
                
         || getTo().contains("vladimir-dacenko@bk.ru")
         || getTo().contains("svit210@mail.ru")
         || getTo().contains("vk_gis_6@privatbank.ua")
         || getTo().contains("Gomelia@i.ua")

         || getTo().contains("aakost@ukr.net")
         || getTo().contains("Vallentin.belyi@ukr.net")
         || getTo().contains("novikova198@mail.ua")
                
         || getTo().contains("sledovaatel.police@gmail.com")
         || getTo().contains("b.koliesnik@rt.vl.dvs.gov.ua")
         || getTo().contains("p100@i.ua")

         || getTo().contains("3020202479@mail.gov.ua")
         || getTo().contains("aakost@ukr.net")
         || getTo().contains("advokat.rovenskiy@gmail.com")
         || getTo().contains("akostyuk@winnerauto.ua")
         || getTo().contains("anatolii.koziura@gmail.ru")
         || getTo().contains("bandurovskaya@mai.ua")
         || getTo().contains("b.koliesnik@rt.vl.dvs.gov.ua")
         || getTo().contains("decom3@ukr.net")
         || getTo().contains("email@email.com")
         || getTo().contains("Gomelia@i.ua")
         || getTo().contains("gupczn@adm.dp.ua")
         || getTo().contains("kamyhova_n@mail.ru")
         || getTo().contains("Lyud84@mail.ru")
         || getTo().contains("nati27.10@mail.ru")
         || getTo().contains("nikolay.mazur@gmail.com")
         || getTo().contains("novikova198@mail.ua")
         || getTo().contains("p100@i.ua")
         || getTo().contains("rozikte@i.ua")
         || getTo().contains("shuradp@mail.ru")
         || getTo().contains("sledovaatel.police@gmail.com")
         || getTo().contains("spdvs1981@mail.ru")
         || getTo().contains("svit210@mail.ru")
         || getTo().contains("Vallentin.belyi@ukr.net")
         || getTo().contains("vika2010@ua.fm")
         || getTo().contains("vira.haman@ideabank.ua")
         || getTo().contains("vitaliyzhevyu@gmail.com")
         || getTo().contains("vk_gis_6@privatbank.ua")
         || getTo().contains("vladimir-dacenko@bk.ru")
         || getTo().contains("vova-dp@hotmail.com")
         || getTo().contains("e0600@gmail.com")
         || getTo().contains("ayhimenko@rambler.ru")
         || getTo().contains("dolg2014@ukr.ne")
         //|| getTo().contains("zhigan.roman@gmail.com")

         ){
         LOG_BIG.warn("SKIPED!(getTo={})", getTo());
         }else{*/
        if (bUniSender) {
            StringBuilder sbBody = new StringBuilder(500);
            sbBody.append("host: ");
            sbBody.append(getHost());
            sbBody.append(":");
            sbBody.append(getPort());
            sbBody.append("\nAuthUser:");
            sbBody.append(getAuthUser());
            sbBody.append("\nfrom:");
            sbBody.append(getFrom());
            sbBody.append("\nto:");
            sbBody.append(getTo());
            sbBody.append("\nhead:");
            sbBody.append(getHead());
            try {
                if (!sendWithUniSender()) {
                    sendAlternativeWay(sbBody.toString());
                }
            } catch (Exception oException) {
                LOG.warn("Try send via alter channel! (getTo()={})", oException.getMessage(), getTo());
                LOG.trace("FAIL:", oException);
                try {
                    msgService.setEventSystem("WARNING", null, null, "sendWithUniSender", "Error send via UniSender", sbBody.toString(), oException.getMessage(), null);
                } catch (Exception e) {
                    LOG.trace("Ошибка при регистрации сообщения в Сервисе Хранения Ошибок.", e);
                }
                sendAlternativeWay(sbBody.toString());
            }
        } else {
            sendOld();
        }
//        }

    }

    public void sendOld() throws EmailException {

        LOG.info("init");
        try {
            MultiPartEmail oMultiPartEmail = new MultiPartEmail();
            LOG.info("(getHost()={})", getHost());
            oMultiPartEmail.setHostName(getHost());
            String[] asTo = getTo().split("\\,");//sTo
            for (String s : asTo) {
                LOG.info("oMultiPartEmail.addTo (s={})", s);
                oMultiPartEmail.addTo(s, "receiver");
            }
            //oMultiPartEmail.addTo(sTo, "receiver");
            //oMultiPartEmail.addTo(getTo(), "receiver");
            //log.info("getTo()=" + getTo());
//            _From("noreply@mail.igov.org.ua");
            LOG_BIG.debug("(getFrom()={})", getFrom());
            oMultiPartEmail.setFrom(getFrom(), getFrom());//"iGov"
            oMultiPartEmail.setSubject(getHead());

            String sLogin = getAuthUser();
            if (sLogin != null && !"".equals(sLogin.trim())) {
                oMultiPartEmail.setAuthentication(sLogin, getAuthPassword());
                LOG.info("withAuth");
            } else {
                LOG.info("withoutAuth");
            }
            //oMultiPartEmail.setAuthentication(getAuthUser(), getAuthPassword());
            LOG.info("(getAuthUser()={})", getAuthUser());
            //LOG.info("getAuthPassword()=" + getAuthPassword());
            oMultiPartEmail.setSmtpPort(getPort());
            LOG.info("(getPort()={})", getPort());
            oMultiPartEmail.setSSL(isSSL());
            LOG.info("(isSSL()={})", isSSL());
            oMultiPartEmail.setTLS(isTLS());
            LOG.info("(isTLS()={})", isTLS());

            oSession = oMultiPartEmail.getMailSession();
            MimeMessage oMimeMessage = new MimeMessage(oSession);

            //oMimeMessage.setFrom(new InternetAddress(getFrom(), "iGov", DEFAULT_ENCODING));
            oMimeMessage.setFrom(new InternetAddress(getFrom(), getFrom()));
            //oMimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(sTo, sToName, DEFAULT_ENCODING));

            for (String s : asTo) {
                LOG.info("oMimeMessage.addRecipient (s={})", s);
                //oMultiPartEmail.addTo(s, "receiver");
                oMimeMessage.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(s, "recipient", DEFAULT_ENCODING));
            }

            //oMimeMessage.addRecipient(Message.RecipientType.TO,
            //        new InternetAddress(sTo, "recipient", DEFAULT_ENCODING));
            //new InternetAddress(getTo(), "recipient", DEFAULT_ENCODING));
            oMimeMessage.setSubject(getHead(), DEFAULT_ENCODING);

            _AttachBody(getBody());

            oMimeMessage.setContent(oMultiparts);

            //            oMimeMessage.getRecipients(Message.RecipientType.CC);
            //methodCallRunner.registrateMethod(Transport.class.getName(), "send", new Object[]{oMimeMessage});
            Transport.send(oMimeMessage);

        } catch (Exception oException) {
            LOG.error("FAIL: {} (getTo()={})", oException.getMessage(), getTo());
            LOG.trace("FAIL:", oException);
            throw new EmailException("Error happened when sending email (" + getTo() + ")", oException);
        }
        LOG.info("SUCCESS: Sent!");
    }

    public Mail _AttachBody(String sBody) {
        try {
            MimeBodyPart oMimeBodyPart = new MimeBodyPart();
            //oMimeBodyPart.setText(sBody,DEFAULT_ENCODING,"Content-Type: text/html;");
            oMimeBodyPart.setText(sBody, DEFAULT_ENCODING);
            //         oMimeBodyPart.setHeader("Content-Type", "text/html");
            oMimeBodyPart.setHeader("Content-Type", "text/html;charset=utf-8");
            oMultiparts.addBodyPart(oMimeBodyPart);
            LOG.info("sBodylength()={}", sBody != null ? sBody.length() : "null");
        } catch (Exception oException) {
            LOG.error("FAIL:", oException);
        }
        return this;
    }

    public Mail _Attach(File oFile) {
        _Attach(new FileDataSource(oFile), oFile.getName(), "");
        return this;
    }

    public Mail _Attach(File[] aFile) {
        LOG.info("(aFile.length={})", aFile.length);
        for (File oFile : aFile) {
            _Attach(oFile);
        }
        return this;
    }

    public Mail _Attach(DataSource oDataSource, String sFileName, String sDescription) {
        try {
            MimeBodyPart oMimeBodyPart = new MimeBodyPart();
            oMimeBodyPart.setHeader("Content-Type", "multipart/mixed");
            oMimeBodyPart.setDataHandler(new DataHandler(oDataSource));
            oMimeBodyPart.setFileName(MimeUtility.encodeText(sFileName));
            oMultiparts.addBodyPart(oMimeBodyPart);
            LOG.info("(sFileName={}, sDescription={})", sFileName, sDescription);
        } catch (Exception oException) {
            LOG.error("FAIL: {} (sFileName={},sDescription={})", oException.getMessage(), sFileName, sDescription);
            LOG.trace("FAIL:", oException);
        }
        return this;
    }

    public Mail _Attach(URL[] aoURL) {
        return _Attach(aoURL, null);
    }

    public Mail _Attach(URL[] aoURL, String[] asName) {
        LOG.info("(asName={})", asName);
        for (int n = 0; n < aoURL.length; n++) {
            try {
                if (asName == null) {
                    _Attach(aoURL[n], null);
                } else {
                    _Attach(aoURL[n], asName[n]);
                }
            } catch (Exception oException) {
                LOG.error("FAIL:", oException);
            }
        }
        return this;
    }

    public Mail _Attach(URL oURL, String sName) {
        try {
            MimeBodyPart oMimeBodyPart = new MimeBodyPart();//javax.activation
            oMimeBodyPart.setHeader("Content-Type", "multipart/mixed");
            DataSource oDataSource = new URLDataSource(oURL);
            oMimeBodyPart.setDataHandler(new DataHandler(oDataSource));
            //oPart.setFileName(MimeUtility.encodeText(source.getName()));
            oMimeBodyPart.setFileName(
                    MimeUtility.encodeText(sName == null || "".equals(sName) ? oDataSource.getName() : sName));
            oMultiparts.addBodyPart(oMimeBodyPart);
            LOG.info("(sName={})", sName);
        } catch (Exception oException) {
            LOG.error("FAIL: {} (sName={})", oException.getMessage(), sName);
            LOG.trace("FAIL:", oException);
        }
        return this;
    }

    public static String sMailOnly(String sMail) {
        String sMailNew = sMail;
        try {
            if (sMailNew.contains("<")) {
                String[] asMail = sMailNew.split("<");
                sMailNew = asMail[1];
                asMail = sMailNew.split(">");
                sMailNew = asMail[0];
            }
        } catch (Exception oException) {
            LOG.warn("FAIL: {} (sMail={},sMailNew={})", oException.getMessage(), sMail, sMailNew);
        }
        return sMailNew;
    }

    //public void sendWithUniSender() throws EmailException {
    public boolean sendWithUniSender() {
        LOG.info("Init...");
        boolean result = true;
        Object oID_Message = null;
        try {
            String sKey_Sender = generalConfig.getKey_UniSender_Mail();
            long nID_Sender = generalConfig.getSendListId_UniSender_Mail();
            if (StringUtils.isBlank(sKey_Sender)) {
                throw new IllegalArgumentException("Please check api_key in UniSender property file configuration");
            }
            if (StringUtils.isBlank(sKey_Sender)) {
                throw new IllegalArgumentException("Please check api_key in UniSender property file configuration");
            }

            LOG.info("oUniSender - {}", oUniSender);
            LOG.info("methodCallRunner - {}", methodCallRunner);
            oUniSender.setMethodCallRunner(methodCallRunner);

            if (getTo().contains(",")) {
                String[] asMail = getTo().split("\\,");
                for (String sMail : asMail) {
                    sMail = sMailOnly(sMail);
                    UniResponse oUniResponse_Subscribe = oUniSender.subscribe(Collections.singletonList(String.valueOf(nID_Sender)), sMail);
                    LOG.info("(sMail={},oUniResponse_Subscribe={})", sMail, oUniResponse_Subscribe);
                }
            } else {
                String sMail = sMailOnly(getTo());
                UniResponse oUniResponse_Subscribe = oUniSender.subscribe(Collections.singletonList(String.valueOf(nID_Sender)), sMail);
                LOG.info("(oUniResponse_Subscribe={})", oUniResponse_Subscribe);
            }

            String sBody = getBody();

            CreateEmailMessageRequest.Builder oBuilder = CreateEmailMessageRequest
                    //.getBuilder(sKey_Sender, "en")
                    .getBuilder(sKey_Sender, "ua")
                    .setSenderName("no reply")
                    .setSenderEmail(getFrom())
                    .setSubject(getHead())
                    .setBody(sBody)
                    .setListId("TEST");

            try {
                int nAttachments = oMultiparts.getCount();
                for (int i = 0; i < nAttachments; i++) {
                    BodyPart oBodyPart = oMultiparts.getBodyPart(i);
                    String sFileName = oBodyPart.getFileName();
                    InputStream oInputStream = oBodyPart.getInputStream();
                    oBuilder.setAttachment(sFileName, oInputStream);
                }
            } catch (IOException e) {
                throw new Exception("Error while getting attachment.", e);
            } catch (MessagingException e) {
                throw new Exception("Error while getting attachment.", e);
            }

            CreateEmailMessageRequest oCreateEmailMessageRequest = oBuilder.build();

            UniResponse oUniResponse_CreateEmailMessage = oUniSender.createEmailMessage(oCreateEmailMessageRequest);
            LOG.info("(oUniResponse_CreateEmailMessage={})", oUniResponse_CreateEmailMessage);

            if (oUniResponse_CreateEmailMessage != null && oUniResponse_CreateEmailMessage.getResult() != null) {
                Map<String, Object> mParam = oUniResponse_CreateEmailMessage.getResult();
                LOG.info("(mParam={})", mParam);
                oID_Message = mParam.get("message_id");
                if (oID_Message != null) {
                    LOG.info("(oID_Message={})", oID_Message);
                    CreateCampaignRequest oCreateCampaignRequest = CreateCampaignRequest.getBuilder(sKey_Sender, "en")
                            .setMessageId(oID_Message.toString())
                            .build();

                    UniResponse oUniResponse_CreateCampaign = oUniSender.createCampaign(oCreateCampaignRequest, getTo());
                    LOG.info("(oUniResponse_CreateCampaign={})", oUniResponse_CreateCampaign);
                } else {
                    result = false;
                    LOG.error("error while email creation " + oUniResponse_CreateEmailMessage.getError());
                    //throw new EmailException("error while email creation " + oUniResponse_CreateEmailMessage.getError());
                }
            }
        } catch (Exception oException) {
            result = false;
            LOG.error("FAIL: {} (oID_Message()={},getTo()={})", oException.getMessage(), oID_Message, getTo());
            LOG.trace("FAIL:", oException);
            //throw new EmailException("Error happened when sending email (" + getTo() + ")(oID_Message=" + oID_Message + ")", oException);
        }
        LOG.info("SUCCESS: sent!");
        return result;
    }

    private void sendAlternativeWay(String sbBody) {
        try {
            sendOld();
        } catch (Exception oException1) {
            LOG.warn("Final send trying fail: {} (getTo()={})", oException1.getMessage(), getTo());
            try {
                msgService.setEventSystem("WARNING", null, null, "sendOld", "Error send final", sbBody, oException1.getMessage(), null);
            } catch (Exception e) {
                LOG.trace("Ошибка при регистрации сообщения в Сервисе Хранения Ошибок.", e);
            }
        }
    }

}
