
This is work in it's early stages.
==================================

If you are looking for examples, take a look at the weld integration
samples. They make use of the err-jms integration module:

	http://github.com/errai/errai-cdi	

The 'cli' package contains a simple client that can be used to send and receive messages.
In order to do so, you need to install JBoss-6.0.0.M2 and deploy the weld integration samples to it.
After that you invoke the client and watch it sending message to the GWT UI:

	./src/main/java/org/jboss/errai/cli/PubSubClient.java


Command line execution
======================
The PubSubClient can be invoked directly from maven:

    mvn -Pclient-demo exec:exec


Have fun,
The Errai Team



