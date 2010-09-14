/* jboss.org */
package org.jboss.errai.jms;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.api.builder.Sendable;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Enumeration;

/**
 * Listens on a JMS Topic and forward message to Errai.
 * Supported JMS messages type is {@link javax.jms.MapMessage}.
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 19, 2010
 */
public class TopicSubscription extends TopicBridge
{

  private static final Logger log = LoggerFactory.getLogger(TopicSubscription.class);

  public TopicSubscription(final MessageBus bus, JMSBinding binding)
  {
    super(binding, bus);

    createSubscription();
  }

  private void createSubscription()
  {
    try
    {      
      TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);

      topicSubscriber.setMessageListener(
          new MessageListener()
          {
            public void onMessage(javax.jms.Message message)
            {

              // dispatch to server message bus
              if(message instanceof MapMessage)
              {
                // addressing
                MessageBuildParms<MessageBuildSendableWithReply> parms = MessageBuilder.createMessage()
                    .toSubject("jms:"+binding.getTopicName())
                    .signalling();

                // payload
                try
                {
                  Enumeration e = ((MapMessage) message).getMapNames();
                  while (e.hasMoreElements())
                  {
                    String item = (String) e.nextElement();
                    Object obj = ((MapMessage) message).getObject(item);
                    parms.with(item, obj);
                  }
                }
                catch (JMSException e)
                {
                  log.error("Error processing message contents", e);
                }

                // dispatch
                parms.noErrorHandling().sendNowWith(bus);
                try
                {
                  message.acknowledge();
                }
                catch (JMSException e)
                {
                  log.error("Failed to acknowledge message", e);
                }
              }
              else
              {
                log.error("Unbale to process message type "+ message);
              }
            }
          }
      );

    }
    catch (Exception e)
    {
      log.error("Faile to create JMS binding", e );
    }
  } 
}
