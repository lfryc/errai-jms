/* jboss.org */
package org.jboss.errai.jms;

import org.jboss.errai.bus.client.framework.MessageBus;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 19, 2010
 */
public abstract class TopicBridge implements JMSBridge
{
  protected final MessageBus bus;
  protected final JMSBinding binding;
  protected TopicConnection topicConnection;
  protected TopicSession topicSession;
  protected TopicConnectionFactory topicConnectionFactory;
  protected Topic topic;
  
  private static final String JNDI_NAME = "ConnectionFactory";

  public TopicBridge(JMSBinding binding, final MessageBus bus)
  {
    this.binding= binding;
    this.bus = bus;

    initConnection();

  }

  protected void initConnection()
  {
    try
    {
      InitialContext jndiContext = new InitialContext();

      // create a subscription
      topicConnectionFactory =
          (TopicConnectionFactory) jndiContext.lookup(JNDI_NAME);
      topic = (Topic) jndiContext.lookup(binding.getTopicName());


      topicConnection = topicConnectionFactory.createTopicConnection();
      topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to prepare topic connection", e);
    }
  }

  public JMSBinding getBinding()
  {
    return binding;
  }

  public void activate()
  {
    try
    {
      if(topicConnection==null)
        initConnection();

      topicConnection.start();
    }
    catch (JMSException e)
    {
      throw new RuntimeException("Failed to start topic connection", e);
    }
  }

  public void deactivate()
  {
    try
    {
      if(topicConnection!=null)
        topicConnection.close();
    }
    catch (JMSException e)
    {
      throw new RuntimeException("Faile to close topic connection", e);
    }
  }
}
