package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang.StringUtils;

import com.defenestrate.chukkars.client.AdminService;
import com.defenestrate.chukkars.server.entity.MessageAdmin;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.defenestrate.chukkars.shared.Player;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.Column;
import com.google.gdata.data.spreadsheet.Data;
import com.google.gdata.data.spreadsheet.Field;
import com.google.gdata.data.spreadsheet.Header;
import com.google.gdata.data.spreadsheet.RecordEntry;
import com.google.gdata.data.spreadsheet.RecordFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.TableEntry;
import com.google.gdata.data.spreadsheet.TableFeed;
import com.google.gdata.data.spreadsheet.Worksheet;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
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
	@Override
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
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to get email settings:\n" + e.getMessage(),
				e);

		    throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}

		return ret;
	}

	@Override
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
				String msg = "MessageAdmin not found with id = " + data.getId();
				IllegalArgumentException e = new IllegalArgumentException(msg);
				throw e;
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

		    throw new RuntimeException(e);
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

	@Override
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

			//------------------

			List<Day> worksheetTitles = getActiveDays();

			for(Day currTitle : worksheetTitles)
			{
				//calculate # of game chukkars
				int totalChukkars = 0;
				int numPlayersPerDay = 0;
				int maxPlayerNumChukkars = 0;
				for(Player currPlayer : allPlayersList)
				{
					if(currPlayer.getRequestDay() == currTitle)
					{
						totalChukkars += currPlayer.getChukkarCount();
						numPlayersPerDay++;

						if( maxPlayerNumChukkars < currPlayer.getChukkarCount() )
						{
							maxPlayerNumChukkars = currPlayer.getChukkarCount();
						}
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


				if(numGameChukkars < maxPlayerNumChukkars)
				{
					numGameChukkars = maxPlayerNumChukkars;
				}


				try
				{
					//delete old lineup
					URL worksheetFeedUrl = lineupsEntry.getWorksheetFeedUrl();
					WorksheetFeed worksheetFeed = service.getFeed(worksheetFeedUrl, WorksheetFeed.class);
					for( WorksheetEntry currWorksheet : worksheetFeed.getEntries() )
					{
						if( currWorksheet.getTitle().getPlainText().equalsIgnoreCase(currTitle.toString()) )
						{
							currWorksheet.delete();
							break;
						}
					}

					//create a new blank worksheet for the new lineup
					WorksheetEntry newWorksheet = new WorksheetEntry();
					newWorksheet.setTitle( new PlainTextConstruct(currTitle.toString()) );
					newWorksheet.setRowCount(numPlayersPerDay + 10);
					newWorksheet.setColCount(numGameChukkars + 3);
					service.insert(worksheetFeedUrl, newWorksheet);

					//populate with players and chukkars
					TableEntry tableEntry = new TableEntry();

					FeedURLFactory factory = FeedURLFactory.getDefault();
					URL tableFeedUrl = factory.getTableFeedUrl(lineupsEntry.getKey());

					// Specify a basic table:
					tableEntry.setTitle( new PlainTextConstruct(currTitle.toString() + " Lineup Table") );
					tableEntry.setWorksheet( new Worksheet(currTitle.toString()) );
					tableEntry.setHeader( new Header(1) );

					// Specify columns in the table, start row, number of rows.
					Data tableData = new Data();
					//for now tableData represents the table header columns, so
					//there are technically "0" rows of data afterwards. This is
					//done so that when we start adding record entries, the data
					//rows will be added directly under the table header columns
					tableData.setNumberOfRows(0);
					// Start row index cannot overlap with header row.
					tableData.setStartIndex(2);

					// populate the table header columns
					tableData.addColumn( new Column("A", "Name") );

					char currCol='B';
					int numChukkarCols = numGameChukkars + 1;
					for(int i=1; i<=numChukkarCols; i++)
					{
						tableData.addColumn( new Column(Character.toString(currCol++), Integer.toString(i)) );
					}

					tableEntry.setData(tableData);
					TableEntry insertedTable = service.insert(tableFeedUrl, tableEntry);

					//--------------------

					//populate with players and chukkars
					String[] parts = insertedTable.getId().split("\\/");
					String tableId = parts[parts.length - 1];
					URL recordFeedUrl = factory.getRecordFeedUrl(lineupsEntry.getKey(), tableId);

					RecordEntry colorEntry = new RecordEntry();
					colorEntry.addField( new Field(null, "Name", "light") );
					padRemainingColumns(colorEntry, 1, numChukkarCols);
					service.insert(recordFeedUrl, colorEntry);

					for(int i=0, n=allPlayersList.size(), halfWay=numPlayersPerDay/2, currPlayerCount=0;
						i<n;
						i++)
					{
						if(currPlayerCount == halfWay)
						{
							//insert 2 blank rows
							for(int j=0; j<2; j++)
							{
								RecordEntry blankEntry = new RecordEntry();
								blankEntry.addField( new Field(null, "Name", " ") );
								padRemainingColumns(blankEntry, 1, numChukkarCols);
								service.insert(recordFeedUrl, blankEntry);
							}

							colorEntry = new RecordEntry();
							colorEntry.addField( new Field(null, "Name", "dark") );
							padRemainingColumns(colorEntry, 1, numChukkarCols);
							service.insert(recordFeedUrl, colorEntry);

							//get counter variable off of "halfway" mark
							currPlayerCount++;
						}


						Player currPlayer = allPlayersList.get(i);

						if(currPlayer.getRequestDay() == currTitle)
						{
							if(currPlayer.getChukkarCount() > 0)
							{
								RecordEntry playerEntry = new RecordEntry();
								playerEntry.addField( new Field(null, "Name", currPlayer.getName()) );

								int j=1;
								for(int m=currPlayer.getChukkarCount(); j<=m; j++)
								{
									playerEntry.addField( new Field(null, Integer.toString(j), "x") );
								}

								padRemainingColumns(playerEntry, j, numChukkarCols);

								service.insert(recordFeedUrl, playerEntry);
							}

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

	private List<Day> getActiveDays()
	{
		Day[] allDays = Day.getAll();
		List<Day> retList = new ArrayList<Day>();

		for(Day currDay : allDays)
		{
			if( currDay.isEnabled() )
			{
				retList.add(currDay);
			}
		}

		return retList;
	}

	private void padRemainingColumns(RecordEntry entry, int colStart, int colEnd)
	{
		for(int i=colStart; i<=colEnd; i++)
		{
			entry.addField( new Field(null, Integer.toString(i), " ") );
		}
	}

	@Override
	public String importLineup(Day dayOfWeek)
	{
		try
		{
			SpreadsheetService service = getDefaultSpreadsheetService();
			SpreadsheetEntry lineupsEntry = getLineupsEntry();
			FeedURLFactory factory = FeedURLFactory.getDefault();
			URL tableFeedUrl = factory.getTableFeedUrl(lineupsEntry.getKey());
			TableFeed feed = service.getFeed(tableFeedUrl, TableFeed.class);
			TableEntry lineupTblEntry = null;

			for( TableEntry entry : feed.getEntries() )
			{
				if( entry.getTitle().getPlainText().equalsIgnoreCase(dayOfWeek.toString() + " Lineup Table") )
				{
					lineupTblEntry = entry;
					break;
				}
			}

			String[] parts = lineupTblEntry.getId().split("\\/");
			String tableId = parts[parts.length - 1];
			URL recordFeedUrl = factory.getRecordFeedUrl(lineupsEntry.getKey(), tableId);
			RecordFeed recordFeed = service.getFeed(recordFeedUrl, RecordFeed.class);

			StringBuffer strBuf = new StringBuffer();

			for( RecordEntry entry : recordFeed.getEntries() )
			{
				List<Field> fieldList = entry.getFields();
				Collections.sort(fieldList, new Comparator<Field>()
				{
					@Override
					public int compare(Field f1, Field f2)
					{
						boolean isF1Digit = Character.isDigit( f1.getName().charAt(0) );
						boolean isF2Digit = Character.isDigit( f2.getName().charAt(0) );

						if(!isF1Digit && isF2Digit)
						{
							return -1;
						}
						else if(isF1Digit && !isF2Digit)
						{
							return 1;
						}
						else
						{
							return new Integer(Integer.parseInt(f1.getName())).compareTo(
								new Integer(Integer.parseInt(f2.getName())) );
						}
					}

					@Override
					public boolean equals(Object o)
					{
						return super.equals(o);
					}
				});

				for(Field currField : fieldList)
				{
					if( currField.getName().equalsIgnoreCase("Name") )
					{
						strBuf.append(currField.getValue() != null ? currField.getValue() : "");
						strBuf.append("\t");
					}
					else if( StringUtils.isNotBlank(currField.getValue()) )
					{
						strBuf.append( currField.getName() );
						strBuf.append(",");
					}
				}

				if(strBuf.charAt(strBuf.length()-1) == ',')
				{
					strBuf.setLength(strBuf.length() - 1);
				}

				strBuf.append("\n");
			}


			return strBuf.toString();
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

	@Override
	public void createAdminUser(String emailAddress, String nickname)
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		Transaction tx = pm.currentTransaction();

		try
		{
		    tx.begin();

		    LoginInfo loginInfo = new LoginInfo();
			loginInfo.setIsAdmin(true);
			loginInfo.setEmailAddress(emailAddress);
			loginInfo.setNickname(nickname);

		    MessageAdmin persistData = new MessageAdmin();
		    persistData.setAdmin(loginInfo);
		    ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
		    persistData.setRecipientEmailAddress( strings.getString("clubListEmail") );
			persistData.setWeeklyEmailsEnabled(false);
			persistData.setSignupNoticeMessage("");
			persistData.setSignupReminderMessage("");

			pm.makePersistent(persistData);

		    tx.commit();
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to create a new admin: (" + emailAddress + ", " + nickname + ")",
				e);

		    if( tx.isActive() )
		    {
		        tx.rollback();
		    }

		    throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}
	}

	@Override
	public Boolean doAdminsExist()
	{
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	    com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("MessageAdmin").setKeysOnly();
	    PreparedQuery pq = ds.prepare(q);
	    List<Entity> results = pq.asList(FetchOptions.Builder.withLimit(1));

		return !results.isEmpty();
	}
}