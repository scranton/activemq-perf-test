Virtual Topic Performance Tuning
================================

The Network of Brokers configuration allows you to scale out the processing of messages across multiple brokers. In
this configuration, messages are forwarded from the broker the producer sends the messages to the networked broker that
the consumer is connected to. The clients have been configured to connect to different brokers.

The Producer will send 10,000 14K random text messages to a virtual topic. The Consumer will start 10 connections in
different threads listening on the corresponding queues for that virtual topic. 

To run this sample,

First time running the sample, build and package everything by running:

    shell> mvn clean install

Start the first master broker in a shell:

    shell1> <activemq_home>/bin/activemq console xbean:file:conf/activemq-ms1a.xml

Start the first slave broker in a shell:

    shell2> <activemq_home>/bin/activemq console xbean:file:conf/activemq-ms1b.xml

Start the second master broker in another shell:

    shell3> <activemq_home>/bin/activemq console xbean:file:conf/activemq-ms2a.xml

Start the second slave broker in another shell:

    shell4> <activemq_home>/bin/activemq console xbean:file:conf/activemq-ms2b.xml

Alternatively you can start ActiveMQ from Maven:

    shell1> mvn -P broker-ms1a
    shell2> mvn -P broker-ms1b
    shell3> mvn -P broker-ms2a
    shell4> mvn -P broker-ms2b

Start the message consumer in another shell:

    shell5> mvn -P consumer

Start the message producer in another shell:

    shell6> mvn -P producer