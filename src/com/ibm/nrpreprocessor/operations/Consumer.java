package com.ibm.nrpreprocessor.operations;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.jms.*;
import javax.naming.*;

/** This class consumes the New Relic Response data in the hornetQ on  the Wildfly AS */
public class Consumer implements MessageListener, ExceptionListener {

    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private Connection connection = null;
    private Context context;

    /** This is a hack to access properties files when debugging*/
    protected boolean debug = true;

    public void Consume(Consumer asyncReceiver) throws Throwable {

        try {
            /** Get the initial context */
            final Properties props = new Properties();
            /** If debugging in IDE the properties are acceded this way */
            if(debug){
                InputStream f = getClass().getClassLoader().getResourceAsStream("consumer.properties");
                props.load(f);
            }
            /** If running the .jar artifact the properties are acceded this way*/
            else{
                File jarPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
                String propertiesPath = jarPath.getParentFile().getAbsolutePath();
                props.load(new FileInputStream(propertiesPath + File.separator + "consumer.properties"));
            }

            /** These few lines should be removed and setup in the properties file*/
            props.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty("INITIAL_CONTEXT_FACTORY"));
            props.put(Context.PROVIDER_URL, props.getProperty("PROVIDER_URL"));
            props.put(Context.SECURITY_PRINCIPAL, props.getProperty("DEFAULT_USERNAME"));
            props.put(Context.SECURITY_CREDENTIALS, props.getProperty("DEFAULT_PASSWORD"));
            context = new InitialContext(props);

            /** Lookup the queue object */
            Queue queue = (Queue) context.lookup(props.getProperty("DEFAULT_DESTINATION"));

            /** Lookup the queue connection factory */
            ConnectionFactory connFactory = (ConnectionFactory) context.lookup(props.getProperty("DEFAULT_CONNECTION_FACTORY"));

             /** Create a queue connection */
            connection = connFactory.createConnection(props.getProperty("DEFAULT_USERNAME"), props.getProperty("DEFAULT_PASSWORD"));

             /** Create a queue session */
            Session queueSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

             /** Create a queue consumer */
            MessageConsumer msgConsumer = queueSession.createConsumer(queue);

             /** Set an asynchronous message listener */
            msgConsumer.setMessageListener(asyncReceiver);

             /** Set an asynchronous exception listener on the connection */
            connection.setExceptionListener(asyncReceiver);

            /** Start connection */
            connection.start();

             /** Wait for messages */
            System.out.println("waiting for messages");
            for (int i = 0; i < 47483647; i++) {
                Thread.sleep(1000);
                System.out.print(".");
            }
            System.out.println();

        } catch (Exception e) {
            log.severe(e.getMessage());
            throw e;
        }
    }

    @Override
    public void onMessage(Message message) {
        TextMessage msg = (TextMessage) message;
        try {
//            System.out.println("received: " + msg.getText());
            Parser parser = new Parser();
            parser.parseApplication(msg.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onException(JMSException exception) {
        System.err.println("an error occurred: " + exception);
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (context != null) {
                context.close();
            }
            /**  Closing the connection takes care of the session, producer, and consumer */
            if (connection != null) {
                connection.close();
            }
        }
        finally {
            super.finalize();
        }
    }
}
