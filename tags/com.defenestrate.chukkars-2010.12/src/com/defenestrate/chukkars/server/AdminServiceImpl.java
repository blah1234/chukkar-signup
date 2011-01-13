package com.defenestrate.chukkars.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.defenestrate.chukkars.client.AdminService;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AdminServiceImpl extends RemoteServiceServlet 
							  implements AdminService 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( AdminServiceImpl.class.getName() );


	///////////////////////////////// METHODS //////////////////////////////////
	public MessageAdminClientCopy getMessageData(LoginInfo currLogin) 
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
		
		MessageAdminClientCopy ret = null;
		
		try 
		{
			Query q = pm.newQuery(MessageAdmin.class);
			q.setFilter("_loginInfo == currLogin");
			q.declareImports("import com.defenestrate.chukkars.shared.LoginInfo");
			q.declareParameters("LoginInfo currLogin");
			q.setUnique(true);
			MessageAdmin data = (MessageAdmin)q.execute(currLogin);
			
			if(data != null) 
			{
				ret = new MessageAdminClientCopy( data.getId(), 
												  data.isWeeklyEmailsEnabled(), 
												  data.getRecipientEmailAddress(), 
												  data.getSignupNoticeMessage(), 
												  data.getSignupReminderMessage() );
			}
		}
		finally 
		{
			pm.close();
		}
		
		return ret;
	}
	
	public void saveMessageData(MessageAdminClientCopy data)
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
			
		Transaction tx = pm.currentTransaction();

		try
		{
		    tx.begin();

		    MessageAdmin persistData = pm.getObjectById( MessageAdmin.class, data.getId() );

			if(persistData != null) 
			{
				persistData.setRecipientEmailAddress( data.getRecipientEmailAddress() );
				persistData.setSignupNoticeMessage( data.getSignupNoticeMessage() );
				persistData.setSignupReminderMessage( data.getSignupReminderMessage() );
				persistData.setWeeklyEmailsEnabled( data.isWeeklyEmailsEnabled() );
			}
			else
			{
				LOG.log( Level.SEVERE, 
						 "MessageAdmin not found with id = " + data.getId(), 
						 new Throwable().fillInStackTrace() );
			}

		    tx.commit();
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to save the state of MessageAdmin with id = " + data.getId() + ":\n" + e.getMessage(), 
				e);
			
		    if( tx.isActive() )
		    {
		        tx.rollback();
		    }
		}
		finally 
		{
			pm.close();
		}
	}
}