/*
 * Copyright (C) Red Hat, Inc.
 * http://www.redhat.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusebyexample.activemq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

public class SimpleConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleConsumer.class);

    private static final Boolean NON_TRANSACTED = false;
    private static final String CONNECTION_FACTORY_NAME = "myJmsFactory";
    private static final String DESTINATION_NAME = "topic/simple";
    private static final int MESSAGE_TIMEOUT_MILLISECONDS = 120000;
    private static final int NUM_MESSAGES_TO_BE_SENT = Integer.getInteger("test.num.messages.sent", 100);
    private static final int NUM_CONSUMERS = Integer.getInteger("test.num.consumers", 1);

    public static void main(String args[]) {
        try {
            // JNDI lookup of JMS Connection Factory and JMS Destination
            final Context context = new InitialContext();
            final ConnectionFactory factory = (ConnectionFactory) context.lookup(CONNECTION_FACTORY_NAME);

            char clientIdChar = 'A';

            for (int j = 0; j < NUM_CONSUMERS; j++) {
                final String clientId = String.valueOf(clientIdChar++);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection connection = null;
                        try {
                            connection = factory.createConnection();
                            connection.setClientID(clientId);
                            connection.start();

                            Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);

                            //Destination destination = (Destination) context.lookup(DESTINATION_NAME);
                            Destination destination = session.createQueue("Consumers." + clientId + ".VirtualTopic.test.topic.simple");
                            MessageConsumer consumer = session.createConsumer(destination);

                            LOG.info("Start consuming messages from {} with {}ms timeout", destination.toString(), MESSAGE_TIMEOUT_MILLISECONDS);

                            long start = 0;

                            // Synchronous message consumer
                            for (int i = 1; i <= NUM_MESSAGES_TO_BE_SENT; i++) {
                                Message message = consumer.receive(MESSAGE_TIMEOUT_MILLISECONDS);
                                if (message != null) {
                                    if (i == 1) {
                                        start = System.currentTimeMillis();
                                    }
                                    if (message instanceof TextMessage) {
                                        TextMessage textMessage = (TextMessage) message;
                                        String text = textMessage.getText();
                                        int messageId = textMessage.getIntProperty("test.id");
                                        if (i != messageId) {
                                            LOG.error("Consumer {}: Message Ordering issue: expected {}; got {}", clientId, i, messageId);
                                        }
                                        LOG.debug("Consumer {} Got {}", clientId, messageId);
                                    }
                                } else {
                                    break;
                                }
                            }
                            final long end = System.currentTimeMillis();

                            LOG.info("Consumer {}: Duration = {}ms; Messages per second = {}", clientId, (end - start), (NUM_MESSAGES_TO_BE_SENT / ((end - start) / 1000.0)));

                            consumer.close();
                            session.close();
                        } catch (Throwable t) {
                            LOG.error("Unexpected exception ", t);
                        } finally {
                            // Cleanup code
                            // In general, you should always close producers, consumers,
                            // sessions, and connections in reverse order of creation.
                            // For this simple example, a JMS connection.close will
                            // clean up all other resources.
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (JMSException e) {
                                    LOG.error("Exception closing connection", e);
                                }
                            }
                        }
                    }
                }).start();
            }
        } catch (Throwable t) {
            LOG.error("Unexpected exception ", t);
        }
    }
}