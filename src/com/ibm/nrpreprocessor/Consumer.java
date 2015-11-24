package com.ibm.nrpreprocessor;

/**
 * Created by Cloud on 20/11/2015.
 */

import java.util.Properties;
import java.util.logging.Logger;
import javax.jms.*;
import javax.naming.*;

public class Consumer implements MessageListener {

    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String DEFAULT_DESTINATION = "java:/jms/queue/demoQueue";
    private static final String DEFAULT_USERNAME = "jmsuser";
    private static final String DEFAULT_PASSWORD = "babog2001";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL = "http-remoting://johnkiernan.ie:80";
    private Connection connection = null;
    private Context context = null;
    private Queue remoteQueue;
    private Session remoteQueueSession;

    public static void main(String agrs[]) throws Throwable {
        Consumer remoteQInteractor = new Consumer();
        for (int i = 0; i < 200; i++) {
            remoteQInteractor.onMessage(remoteQInteractor.receiveTextMessage());
        }
        remoteQInteractor.finalize();
    }


    public Consumer() throws Throwable {

        try {
            /**
             *Set up the context for the JNDI
             */
            final Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            props.put(Context.PROVIDER_URL, PROVIDER_URL);
            props.put(Context.SECURITY_PRINCIPAL, DEFAULT_USERNAME);
            props.put(Context.SECURITY_CREDENTIALS, DEFAULT_PASSWORD);
            context = new InitialContext(props);


            ConnectionFactory remoteQueueCF = (ConnectionFactory) context.lookup(DEFAULT_CONNECTION_FACTORY);
            remoteQueue = (Queue) context.lookup(DEFAULT_DESTINATION);
            connection = remoteQueueCF.createConnection(DEFAULT_USERNAME, DEFAULT_PASSWORD);
            connection.start();
            remoteQueueSession = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

        } catch (Exception e) {
            log.severe(e.getMessage());
            throw e;
        }
    }

    public Message receiveTextMessage() throws JMSException {
        MessageConsumer msgConsumer = remoteQueueSession.createConsumer(this.remoteQueue);
        return msgConsumer.receive();
    }


    @Override
    public void onMessage(Message inMessage) {
        TextMessage msg = null;

        try {
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                log.info("MESSAGE BEAN: Message received: " + msg.getText());
                msg.acknowledge();
            } else {
                log.warning("Message of wrong type: " + inMessage.getClass().getName());
            }
        } catch (Throwable te) {
            te.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {

        if (context != null) {
            context.close();
        }
        /**
         * closing the connection takes care of the session, producer, and consumer
         */
        if (connection != null) {
            connection.close();
        }
        System.out.println("All closed!");
    }


}