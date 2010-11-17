package jms;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

public class JetStreamConnectionFactoryBean 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final String INITIAL_CONTEXT_FACTORY =
        "com.allure.JetStream.jndi.JetStreamInitialContextFactory";
	static private final String CONNECTION_FACTORY = "ConnectionFactory";
	
	
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	static private final Logger _log = Logger.getLogger(JetStreamConnectionFactoryBean.class);


	///////////////////////////////// METHODS //////////////////////////////////
	public Object getObject() throws Exception 
	{
        ConnectionFactory foundConnectionFactory = null;

        try 
        {
            // 1. Create the InitialContext Object used for looking up
            // JMS administered objects on the Fiorano/EMS
            // located on the default host.
            //
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            
            InitialContext ic = new InitialContext(env);

            // lookup factory
            foundConnectionFactory = (ConnectionFactory)ic.lookup(CONNECTION_FACTORY);
        } 
        catch(Exception ex) 
        {
            _log.error("Exception establishing connection to JMS Server:\nConnection Factory JNDI Name: " + 
            	CONNECTION_FACTORY);
            
            throw ex;
        }

        return foundConnectionFactory;
    }

    public Class getObjectType() 
    {
        return ConnectionFactory.class;
    }
}
