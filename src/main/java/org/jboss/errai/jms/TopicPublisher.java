/* jboss.org */
package org.jboss.errai.jms;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * Dispatches messages from Errai to a JMS Topic.
 * Resulting JMS message type is {@link javax.jms.MapMessage}
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 19, 2010
 */
public class TopicPublisher extends TopicBridge {
    private static final Logger log = LoggerFactory.getLogger(TopicSubscription.class);

    public TopicPublisher(MessageBus bus, JMSBinding binding) {
        super(binding, bus);

        createPublisher();
    }

    private void createPublisher() {
        try {
            final javax.jms.TopicPublisher jms = topicSession.createPublisher(topic);
            bus.subscribe("jms:" + getBinding().getTopicName(), new MessageCallback() {
                public void callback(Message message) {
                    // dispatch message to JMS
                    try {
                        MapMessage jmsMessage = topicSession.createMapMessage();
                        for (String key : message.getParts().keySet()) {
                            jmsMessage.setObject(key, message.get(Object.class, key));
                            jms.publish(jmsMessage);
                        }
                    }
                    catch (JMSException e) {
                        throw new RuntimeException("Failed to dispatch JMS message", e);
                    }
                }
            });
        }
        catch (JMSException e) {
            throw new RuntimeException("Failed to create publisher", e);
        }

    }

    public JMSBinding getBinding() {
        return binding;
    }

}
