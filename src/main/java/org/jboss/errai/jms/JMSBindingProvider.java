/* jboss.org */
package org.jboss.errai.jms;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.api.ErraiConfig;
import org.jboss.errai.bus.server.api.ErraiConfigExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 16, 2010
 */
@ExtensionComponent
public class JMSBindingProvider implements ErraiConfigExtension
{
  private static final Logger log = LoggerFactory.getLogger(JMSBindingProvider.class);

  @Inject
  MessageBus bus;
  private static final String JMS_PREFIX = "jms.";

  public void configure(ErraiConfig config)
  {
    try
    {
      List<JMSBinding> jmsBindings = parseConfig();

      // Setup jms adapter      
      for(final JMSBinding jmsBinding : jmsBindings)
      {
        if(jmsBinding.isSender())
        {
          // create a publisher
          log.info("Errai TopicPublisher: {}", jmsBinding.getTopicName());
          new TopicPublisher(bus, jmsBinding).activate();
        }
        else
        {
          // Create subscription
          log.info("Errai TopicSubscription: {}", jmsBinding.getTopicName());
          new TopicSubscription(bus, jmsBinding).activate();
        }
      }
    }
    catch (Exception e)
    {
      log.error("Failed to process errai-jms bindings", e);
    }

  }

  private List<JMSBinding> parseConfig()
  {
    try
    {
      List<JMSBinding> jmsBindings = new ArrayList<JMSBinding>();

      // Parse config
      log.info("Process JMS binding declarations in ErraiApp.properties");
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> cfgs = loader.getResources("ErraiApp.properties");

      while(cfgs.hasMoreElements())
      {
        URL cfg = cfgs.nextElement();

        Properties config = new Properties();
        config.load(cfg.openStream());

        Set<String> processed = new HashSet<String>();
        for(String key : config.stringPropertyNames())
        {
          if(key.startsWith(JMS_PREFIX))
          {
            String suffix = key.substring(key.indexOf(JMS_PREFIX)+4, key.length());
            String distinctKey = JMS_PREFIX + suffix.substring(0, suffix.indexOf("."));
            if(processed.contains(distinctKey))
              continue;
            else
              processed.add(distinctKey);

            jmsBindings.add(
                new JMSBinding(
                    config.getProperty(distinctKey+".topic"),
                    Boolean.valueOf(config.getProperty(distinctKey+".send"))
                )
            );
          }
        }
      }

      log.info("Found {} JMS bindings", jmsBindings.size());

      return jmsBindings;

    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to read JMS binding declarations in ErraiApp.properties");
    }
  }
}
