package ua.lab8.client;

import org.apache.activemq.ActiveMQConnectionFactory;
import ua.lab8.server.AMQServer;

import javax.jms.*;
import java.util.List;
import java.util.stream.Stream;

public class AMQClient implements Client {
    /**
     * Objects used to connect to ActiveMQ
     */
    private final Session session;
    private final Connection connection;
    private final MessageProducer producer;
    private final MessageConsumer responseConsumer;

    /**
     * Queue name that this app will listen for messages from.
     */
    private final String QUEUE_NAME = "channel";
    private Destination adminQueue;
    private Destination tempDest;

    /**
     * Objects used in creating the session
     */
    private final boolean transacted = false;
    private final int ackMode = Session.AUTO_ACKNOWLEDGE;
    private volatile String response = null;

    public AMQClient() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(AMQServer.BROKER_URL);

        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(transacted, ackMode);
            adminQueue = session.createQueue(QUEUE_NAME);
            producer = session.createProducer(adminQueue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            tempDest = session.createTemporaryQueue();
            responseConsumer = session.createConsumer(tempDest);
            responseConsumer.setMessageListener(new MyListener(this));
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setResponse(String response) {
        this.response = response;
    }

    private void sendAndWaitResponse(String message, boolean wait) {
        try {
            TextMessage textMessage = session.createTextMessage(message);
            textMessage.setJMSReplyTo(tempDest);
            producer.send(textMessage);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        if (!wait) return;
        while (response == null) Thread.onSpinWait();
    }

    private String getResult() {
        String result = response;
        response = null;
        return result;
    }

    @Override
    public List<String> queryFolders() {
        sendAndWaitResponse("folders", true);
        List<String> result = Stream.of(response.split("\n")).filter(folderName -> !folderName.equals("end")).toList();
        response = null;
        return result;
    }

    @Override
    public String saveFile(String jsonObj) {
        sendAndWaitResponse("create file\n" + jsonObj, true);
        return getResult();
    }

    @Override
    public String saveFolder(String folderName) {
        sendAndWaitResponse("create folder\n" + folderName, true);
        return getResult();
    }

    @Override
    public String queryDeleteFolder(String folderName) {
        sendAndWaitResponse("delete folder\n" + folderName, true);
        return getResult();
    }

    @Override
    public String queryDeleteFile(String folderName, String fileName) {
        sendAndWaitResponse("delete file\n" + folderName + "/" + fileName, true);
        return getResult();
    }

    @Override
    public void queryUpdateFile(String updateExpression) {
        sendAndWaitResponse("update file\n" + updateExpression, false);
        response = null;
    }

    @Override
    public void queryCopyFile(String copyExpression) {
        sendAndWaitResponse("copy file\n" + copyExpression, false);
        response = null;
    }

    @Override
    public List<String> queryFiles(String folderName) {
        sendAndWaitResponse("files\n" + folderName, true);
        List<String> result = Stream.of(response.split("\n")).filter(fileName -> !fileName.equals("end")).toList();
        response = null;
        return result;
    }

    @Override
    public void close() {
        try {
            sendAndWaitResponse("exit", false);
            session.close();
            connection.close();
            responseConsumer.close();
            producer.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}

class MyListener implements MessageListener {

    private final AMQClient client;

    public MyListener(AMQClient client) {
        this.client = client;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage msg) {
            try {
                client.setResponse(msg.getText());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        } else
            throw new RuntimeException("Unexpected message: " + message);
    }
}
