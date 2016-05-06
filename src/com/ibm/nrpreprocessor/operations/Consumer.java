package com.ibm.nrpreprocessor.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import javax.jms.*;

import javax.naming.*;

/**
 * This class consumes the New Relic Response data in the hornetQ on  the Wildfly AS
 */
public class Consumer implements MessageListener, ExceptionListener {

    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private javax.jms.Connection connection = null;
    private Context context;
    private ArrayList<String> responseList;
    private ArrayList<String> responseListCopy;
    private Instant responseTime;
    private Instant bufferCheck;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Consumer.class.getName());

    public Consumer() throws Exception {

        this.responseList = new ArrayList<>();
    }

    /**
     * This is a hack to access properties files when debugging
     */
    protected boolean debug = false;

    public void Consume(Consumer asyncReceiver) throws Throwable {

        try {
            /** Get the initial context */
            final Properties props = new Properties();
            /** If debugging in IDE the properties are acceded this way */
            if (debug) {
                try (InputStream f = getClass().getClassLoader().getResourceAsStream("consumer.properties")) {
                    props.load(f);
                }catch(Exception e){
                    e.printStackTrace();
                    logger.fatal("Exception in Consume() getting properties file ", e);
                }
            }
            /** If running the .jar artifact the properties are acceded this way*/
            else {
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
            try (javax.jms.Connection connection = connFactory.createConnection(props.getProperty("DEFAULT_USERNAME"), props.getProperty("DEFAULT_PASSWORD"));



                         /** Create a queue session */
                 Session queueSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                 /** Create a queue consumer */
                 MessageConsumer msgConsumer = queueSession.createConsumer(queue)) {


                /** Set an asynchronous message listener */
                msgConsumer.setMessageListener(asyncReceiver);

                /** Set an asynchronous exception listener on the connection */
                connection.setExceptionListener(asyncReceiver);

                /** Start connection */
                connection.start();

                /** Wait for messages */
                // This is a bad way of doing this Possible use a timer instead
                System.out.println("waiting for messages");
                while (true) {

                    Thread.sleep(5000);
                    System.out.print(".");

                    bufferCheck = Instant.now();
                    if(responseList.size() > 0){

                        long push = Duration.between(responseTime, bufferCheck).getSeconds();
                        System.out.println();
                        System.out.println("Seconds till publish " + (60 - push));
                        if (push > 60) {
                            responseListCopy = new ArrayList<>(responseList);
                            responseList.clear();
                            this.buffer(responseListCopy);
                            responseListCopy.clear();
                        }
                    }
                    // Reconnect to broker if connection is lost
                    connection.setExceptionListener(exception -> {
                        log.severe("ExceptionListener triggered: " + exception.getMessage());
                        try {
                            restartJSMConnection();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                            logger.fatal("Exception in Consume() Reconnecting to broker  ", throwable);
                        }
                    });
                }
            }

        } catch (Exception e) {
            log.severe(e.getMessage());
            throw e;
        } finally {
            if (context != null) {
                context.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /** Buffer inserts to the DB */
    public void buffer(ArrayList<String> list) throws Exception {
        System.out.println("Parsing: " + list.size() + " messages");
        Thread.sleep(1000);
        Parser parser = new Parser();
        parser.addList(list);
        parser.pushToDB(parser.parseApplication());
    }

    public void restartJSMConnection() throws Throwable {

        try {
            Consume(this);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.fatal("Exception in restartJSMConnection() Reconnecting to broker  ", throwable);
        }
    }

    @Override
    public void onMessage(Message message) {
        TextMessage msg = (TextMessage) message;

        try {
            responseList.add(msg.getText());
            responseTime = Instant.now();

        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal("Exception in onMessage() adding message to list  ", e);
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
        } finally {
            super.finalize();
        }
    }
}
