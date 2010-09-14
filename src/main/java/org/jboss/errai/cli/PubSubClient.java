/* jboss.org */
package org.jboss.errai.cli;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Simple command line client to demo the JMS integration
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 16, 2010
 */
public class PubSubClient {
    TopicConnection conn = null;
    TopicSession session = null;
    Topic outboundTopic = null;
    Topic inboundTopic = null;
    private static final String JNDI_HOST = "jnp://localhost:1099";
    private static final String INBOUND_TOPIC = "topic/inboundTopic";
    private static final String OUTBOUND_TOPIC = "topic/outboundTopic";

    public static class ExListener implements MessageListener {
        public void onMessage(Message msg) {
            MapMessage tm = (MapMessage) msg;
            try {
                tm.acknowledge();
            }
            catch (JMSException e) {
                System.err.println("ERR: " + e.getMessage());
            }
            try {
                System.out.println("");
                System.out.println("< " + tm.getString("text"));
                System.out.print("> ");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void setupPubSub()
            throws JMSException, NamingException {
        Properties props = new Properties();
        props.put("java.naming.provider.url", JNDI_HOST);
        props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        InitialContext iniCtx = new InitialContext(props);

        Object tmp = iniCtx.lookup("ConnectionFactory");
        TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
        conn = tcf.createTopicConnection();
        outboundTopic = (Topic) iniCtx.lookup(INBOUND_TOPIC);
        inboundTopic = (Topic) iniCtx.lookup(OUTBOUND_TOPIC);

        session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);

        conn.start();
    }

    public void sendRecvAsync()
            throws JMSException, NamingException {

        greeting();

        // Setup the PubSub connection, session
        setupPubSub();

        // Create subscription
        TopicSubscriber recv = session.createSubscriber(inboundTopic);
        recv.setMessageListener(new ExListener());

        // Create publisher
        TopicPublisher send = session.createPublisher(outboundTopic);

        while (true) {
            MapMessage tm = session.createMapMessage();
            String input = userInput();
            if (input.equals("exit"))
                break;
            tm.setString("text", input);
            send.publish(tm);
        }

        send.close();
        recv.close();

        System.out.println("Connection close");
    }

    private void greeting() {
        System.out.println("\n\n\n === JMS Client Demo ===");
        System.out.println("Connected to: " + JNDI_HOST);
        System.out.println("Listening on: " + OUTBOUND_TOPIC);
        System.out.println("Sending to: " + INBOUND_TOPIC);
        System.out.println("\n\n");
    }

    public void stop() throws JMSException {
        conn.stop();
        session.close();
        conn.close();
    }

    private static String userInput() {
        System.out.print("> ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String message = null;
        try {
            message = br.readLine();
        } catch (IOException ioe) {
            System.out.println("IO error trying to read input");
            System.exit(1);
        }
        return message;
    }

    public static void main(String args[]) throws Exception {
        System.out.println("Begin TopicSendRecvClient, now=" + System.currentTimeMillis());
        PubSubClient client = new PubSubClient();
        client.sendRecvAsync();
        client.stop();
        System.exit(0);
    }
}
