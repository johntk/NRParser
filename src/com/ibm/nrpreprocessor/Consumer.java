package com.ibm.nrpreprocessor;

/**
 * Created by Cloud on 20/11/2015.
 */
import java.util.Properties;
import javax.jms.*;
import javax.naming.*;

public class Consumer implements MessageListener {

    private static final String DEFAULT_USERNAME = "jmsuser";
    private static final String DEFAULT_PASSWORD = "babog2001";
    private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String DEFAULT_DESTINATION = "java:/jms/queue/demoQueue";
    private static final String PROVIDER_URL = "http-remoting://192.168.1.6:8080";
    private Queue remoteQueue;
    private Connection remoteQueueConnection;
    private Session remoteQueueSession;

    public static void main(String agrs[]) throws NamingException, JMSException {
        Consumer remoteQInteractor = new Consumer();
        System.out.println(remoteQInteractor.receiveTextMessage());
    }


    public Consumer() throws NamingException, JMSException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);

        /**
         * The URL below should point to the your instance of Server 1, if no
         * port offset is used for Server 1 the port can remain at 4447
         */
        props.put(Context.PROVIDER_URL, PROVIDER_URL);

        /**
         * Please note that the credentials passed in here have no effect on the
         * messaging system as we have disabled the security on the HornetQ
         * messaging subsystem
         */
        props.put(Context.SECURITY_PRINCIPAL, DEFAULT_USERNAME);
        props.put(Context.SECURITY_CREDENTIALS, DEFAULT_PASSWORD);

        InitialContext ic = new InitialContext(props);

        /**
         * The following two lookups are based on how you configured the
         * RemoteConnectionFactory and the local queue on Server 1. If you have
         * followed the installation that was provided as-is then you can go
         * with the below code
         */
        ConnectionFactory remoteQueueCF = (ConnectionFactory) ic.lookup(DEFAULT_CONNECTION_FACTORY);
        remoteQueue = (Queue) ic.lookup(DEFAULT_DESTINATION);

        remoteQueueConnection = remoteQueueCF.createConnection(DEFAULT_USERNAME, DEFAULT_PASSWORD);
        remoteQueueConnection.start();
        remoteQueueSession = remoteQueueConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
    }

    public String receiveTextMessage() throws JMSException {
        MessageConsumer msgConsumer = remoteQueueSession.createConsumer(this.remoteQueue);
        TextMessage txtMsg = (TextMessage) msgConsumer.receive();
        msgConsumer.close();
        return txtMsg.getText();
    }

    @Override
    protected void finalize() throws Throwable {
        remoteQueueSession.close();
        remoteQueueConnection.close();
    }

    @Override
    public void onMessage(Message message) {

    }
}
