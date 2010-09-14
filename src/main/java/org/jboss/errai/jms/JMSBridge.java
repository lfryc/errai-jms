/* jboss.org */
package org.jboss.errai.jms;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 19, 2010
 */
public interface JMSBridge
{
  JMSBinding getBinding();
  void activate();
  void deactivate();
}
