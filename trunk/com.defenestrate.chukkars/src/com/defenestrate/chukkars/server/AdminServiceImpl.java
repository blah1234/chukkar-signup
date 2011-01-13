package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.defenestrate.chukkars.client.AdminService;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.defenestrate.chukkars.shared.Player;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AdminServiceImpl extends RemoteServiceServlet 
							  implements AdminService 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( AdminServiceImpl.class.getName() );
	

	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private SpreadsheetService _service;


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
	
	private SpreadsheetService getDefaultSpreadsheetService() throws AuthenticationException
	{
		if(_service == null)
		{
			_service = new SpreadsheetService("com.defenesetrate.chukkars");
			_service.setUserCredentials("horseparkpolo@gmail.com", "polo7656");
		}
		
		return _service;
	}
	
	private SpreadsheetEntry getLineupsEntry() throws MalformedURLException,
													  AuthenticationException,
													  ServiceException,
													  IOException
	{
		URL metafeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
		SpreadsheetService service = getDefaultSpreadsheetService();
		SpreadsheetFeed feed = service.getFeed(metafeedUrl, SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();
		SpreadsheetEntry lineupsEntry = null;
		for(int i = 0; i < spreadsheets.size(); i++) 
		{
			SpreadsheetEntry entry = spreadsheets.get(i);
			if("Chukkar lineups".equals(entry.getTitle().getPlainText()) )
			{
				lineupsEntry = entry;
				break;
			}
		}
		
		return lineupsEntry;
	}
	
	public String exportSignups()
	{
		List<Player> allPlayersList = null;
		
		try
		{
			allPlayersList = PlayerServiceImpl.getPlayersImpl();
		}
		catch(Exception e)
		{
			LOG.log(Level.SEVERE,
					"Error getting all signed up players",
					e);
			
			throw new RuntimeException(e);
		}
		
		
		try
		{
			SpreadsheetService service = getDefaultSpreadsheetService();
			SpreadsheetEntry lineupsEntry = getLineupsEntry();
			List<WorksheetEntry> worksheets = lineupsEntry.getWorksheets();
			Day[] worksheetTitles = 
			{
				Day.SATURDAY,
				Day.SUNDAY
			};
			
			for(Day currTitle : worksheetTitles)
			{
				WorksheetEntry currDayEntry = null;
				
				for(int i=0; i<worksheets.size(); i++) 
				{
					WorksheetEntry worksheet = worksheets.get(i);
					String title = worksheet.getTitle().getPlainText();
					if( currTitle.toString().equals(title) )
					{
						currDayEntry = worksheet;
						break;
					}
				}
				
				try
				{
					//calculate # of game chukkars
					int totalChukkars = 0;
					int numPlayersPerDay = 0;
					for(Player currPlayer : allPlayersList)
					{
						if(currPlayer.getRequestDay() == currTitle)
						{
							totalChukkars += currPlayer.getChukkarCount();
							numPlayersPerDay++;
						}
					}
					
					int numGameChukkars;
					if(numPlayersPerDay < 4)
					{
						numGameChukkars = 0;
					}
					else if(numPlayersPerDay >= 6)
					{
						numGameChukkars = totalChukkars / 6;
					}
					else
					{
						numGameChukkars = totalChukkars / 4;
					}
					
					//allocate enough rows and columns for the lineup
					currDayEntry.setColCount(numGameChukkars + 3);
					currDayEntry.setRowCount(numPlayersPerDay + 10);
					currDayEntry.update();
					
					
					//clear existing worksheet
					URL cellFeedUrl = currDayEntry.getCellFeedUrl();
					CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
					for( CellEntry cell : cellFeed.getEntries() ) 
					{
						cell.delete();
					}
					

					//populate with players and chukkars
					for(int i=1; i<=numGameChukkars; i++)
					{
						//indices start at 1
						CellEntry newEntry = new CellEntry( 1, i+1, Integer.toString(i) );
						service.insert(cellFeedUrl, newEntry);
					}
					
					for(int i=0, n=allPlayersList.size(), halfWay=numPlayersPerDay/2, currRow=2, currPlayerCount=0; i<n; i++)
					{
						if(i == 0)
						{
							CellEntry newEntry = new CellEntry(currRow++, 1, "light");
							service.insert(cellFeedUrl, newEntry);
						}
						else if(currPlayerCount == halfWay)
						{
							currRow += 2;

							CellEntry newEntry = new CellEntry(currRow++, 1, "dark");
							service.insert(cellFeedUrl, newEntry);
						}
						
						
						Player currPlayer = allPlayersList.get(i);
												
						if(currPlayer.getRequestDay() == currTitle)
						{
							CellEntry newEntry = new CellEntry( currRow, 1, currPlayer.getName().toString() );
							service.insert(cellFeedUrl, newEntry);
					
							for(int j=0, m=currPlayer.getChukkarCount(), currCol=2; j<m; j++, currCol++)
							{
								newEntry = new CellEntry(currRow, currCol, "x");
								service.insert(cellFeedUrl, newEntry);
							}
							
							currRow++;
							currPlayerCount++;
						}
					}
				}
				catch(ServiceException e)
				{
					LOG.log(
						Level.SEVERE,
						"Error manipulating Google spreadsheet",
						e);
					
					throw new RuntimeException(e);
				}
				catch(IOException e)
				{
					LOG.log(
						Level.SEVERE,
						"Error manipulating Google spreadsheet",
						e);
					
					throw new RuntimeException(e);
				}
			}
			
			return lineupsEntry.getSpreadsheetLink().getHref();
		}
		catch(MalformedURLException e)
		{
			LOG.log(
				Level.SEVERE,
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full considered malformed",
				e);
			
			throw new RuntimeException(e);
		}
		catch(AuthenticationException e)
		{
			LOG.log(
				Level.SEVERE,
				"Unable to authenticate for Google spreadsheets",
				e);
			
			throw new RuntimeException(e);
		}
		catch(ServiceException e)
		{
			LOG.log(
				Level.SEVERE,
				"Error opening \"Chukkar lineups\" Google spreadsheet",
				e);
			
			throw new RuntimeException(e);
		}
		catch(IOException e)
		{
			LOG.log(
				Level.SEVERE,
				"Error opening \"Chukkar lineups\" Google spreadsheet",
				e);
			
			throw new RuntimeException(e);
		}
	}
	
	public String importLineup(Day dayOfWeek)
	{
/*		try
		{
			SpreadsheetEntry lineupsEntry = getLineupsEntry();
			List<WorksheetEntry> worksheets = lineupsEntry.getWorksheets();
			WorksheetEntry currDayEntry = null;
			
			for(int i=0; i<worksheets.size(); i++) 
			{
				WorksheetEntry worksheet = worksheets.get(i);
				String title = worksheet.getTitle().getPlainText();
				if( dayOfWeek.toString().equals(title) )
				{
					currDayEntry = worksheet;
					break;
				}
			}
				
			try
			{
				int rowCount = currDayEntry.getRowCount();
				int colCount = currDayEntry.getColCount();
				
				
				URL recordFeedUrl = tableEntry.getRecordFeedUrl();
				RecordFeed feed = service.getFeed(recordFeedUrl, RecordFeed.class);
				for( RecordEntry entry : feed.getEntries() ) {
				  System.out.println("Title: " + entry.getTitle().getPlainText());
				  for (Field field : entry.getFields()) {
				    System.out.println("<field name=" + field.getName() + ">"
				      + field.getValue() + "</field>");
				  }
				}
				
				//clear existing worksheet
				URL cellFeedUrl = currDayEntry.getCellFeedUrl();
				CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
				for( CellEntry cell : cellFeed.getEntries() ) 
				{
					cell.delete();
				}
				

				//populate with players and chukkars
				for(int i=1; i<=numGameChukkars; i++)
				{
					//indices start at 1
					CellEntry newEntry = new CellEntry( 1, i+1, Integer.toString(i) );
					service.insert(cellFeedUrl, newEntry);
				}
				
				for(int i=0, n=allPlayersList.size(), halfWay=numPlayersPerDay/2, currRow=2, currPlayerCount=0; i<n; i++)
				{
					if(i == 0)
					{
						CellEntry newEntry = new CellEntry(currRow++, 1, "light");
						service.insert(cellFeedUrl, newEntry);
					}
					else if(currPlayerCount == halfWay)
					{
						currRow += 2;

						CellEntry newEntry = new CellEntry(currRow++, 1, "dark");
						service.insert(cellFeedUrl, newEntry);
					}
					
					
					Player currPlayer = allPlayersList.get(i);
											
					if(currPlayer.getRequestDay() == currTitle)
					{
						CellEntry newEntry = new CellEntry( currRow, 1, currPlayer.getName().toString() );
						service.insert(cellFeedUrl, newEntry);
				
						for(int j=0, m=currPlayer.getChukkarCount(), currCol=2; j<m; j++, currCol++)
						{
							newEntry = new CellEntry(currRow, currCol, "x");
							service.insert(cellFeedUrl, newEntry);
						}
						
						currRow++;
						currPlayerCount++;
					}
				}
			}
			catch(ServiceException e)
			{
				LOG.log(
					Level.SEVERE,
					"Error manipulating Google spreadsheet",
					e);
				
				throw new RuntimeException(e);
			}
			catch(IOException e)
			{
				LOG.log(
					Level.SEVERE,
					"Error manipulating Google spreadsheet",
					e);
				
				throw new RuntimeException(e);
			}
		
		return lineupsEntry.getSpreadsheetLink().getHref();
		}
		catch(MalformedURLException e)
		{
			LOG.log(
				Level.SEVERE,
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full considered malformed",
				e);
			
			throw new RuntimeException(e);
		}
		catch(AuthenticationException e)
		{
			LOG.log(
				Level.SEVERE,
				"Unable to authenticate for Google spreadsheets",
				e);
			
			throw new RuntimeException(e);
		}
		catch(ServiceException e)
		{
			LOG.log(
				Level.SEVERE,
				"Error opening \"Chukkar lineups\" Google spreadsheet",
				e);
			
			throw new RuntimeException(e);
		}
		catch(IOException e)
		{
			LOG.log(
				Level.SEVERE,
				"Error opening \"Chukkar lineups\" Google spreadsheet",
				e);
			
			throw new RuntimeException(e);
		}*/
		return "";
	}
}