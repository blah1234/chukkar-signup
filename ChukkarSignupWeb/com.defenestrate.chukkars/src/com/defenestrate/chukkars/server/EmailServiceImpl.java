package com.defenestrate.chukkars.server;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;

import com.defenestrate.chukkars.client.EmailService;
import com.defenestrate.chukkars.server.entity.MessageAdmin;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EmailServiceImpl extends RemoteServiceServlet
							  implements EmailService
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( EmailServiceImpl.class.getName() );


	///////////////////////////////// METHODS //////////////////////////////////
	static public void sendEmail(String subject, String msgBody, MessageAdmin data, boolean useClubListAddressAsReplyTo) throws ServletException
	{
		sendEmail(data.getRecipientEmailAddress(), subject, msgBody, data, useClubListAddressAsReplyTo);
	}

	static public void sendEmail(String recipientAddress, String subject, String msgBody, MessageAdmin data, boolean useClubListAddressAsReplyTo) throws ServletException
	{
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try
        {
            Message msg = new MimeMessage(session);
            msg.setFrom( new InternetAddress(data.getAdmin().getEmailAddress(), data.getAdmin().getNickname()) );

            if(useClubListAddressAsReplyTo)
            {
            		msg.setReplyTo( new Address[] {new InternetAddress(data.getRecipientEmailAddress())} );
            }

            msg.addRecipient( Message.RecipientType.TO, new InternetAddress(recipientAddress) );
            msg.setSubject(subject);
            msg.setContent(msgBody.replace("\n", "<br>"), "text/html");
            Transport.send(msg);
        }
        catch(AddressException e)
        {
        		throw new ServletException(e);
        }
        catch(MessagingException e)
        {
        		throw new ServletException(e);
        }
        catch(UnsupportedEncodingException e)
        {
        		throw new ServletException(e);
        }
	}

	@Override
	public void sendEmail(String recipientAddress, String subject, String msgBody, LoginInfo data)
	{
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try
        {
            Message msg = new MimeMessage(session);
            msg.setFrom( new InternetAddress(data.getEmailAddress(), data.getNickname()) );
            msg.addRecipient( Message.RecipientType.TO, new InternetAddress(recipientAddress) );
            msg.setSubject(subject);
            msg.setText(msgBody);
            Transport.send(msg);
        }
        catch(AddressException e)
        {
	        	LOG.log(Level.SEVERE,
	    				"Error constructing recipient email address: " + recipientAddress,
	    				e);

	        	throw new RuntimeException(e);
        }
        catch(MessagingException e)
        {
	        	LOG.log(Level.SEVERE,
	    				"Error constructing or sending email message",
	    				e);

	        	throw new RuntimeException(e);
        }
        catch(UnsupportedEncodingException e)
        {
	        	LOG.log(Level.SEVERE,
	    				"Error constructing sender email address: " + data.getEmailAddress(),
	    				e);

	        	throw new RuntimeException(e);
        }
	}
}
