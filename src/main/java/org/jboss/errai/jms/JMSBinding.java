/* jboss.org */
package org.jboss.errai.jms;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 16, 2010
 */
public final class JMSBinding
{
  String topicName;
  boolean sender;

  public JMSBinding(String topicName, boolean sender)
  {
    this.topicName = topicName;
    this.sender = sender;
  }

  public String getTopicName()
  {
    return topicName;
  }

  public boolean isSender()
  {
    return sender;
  }
}
