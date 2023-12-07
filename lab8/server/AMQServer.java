package ua.lab8.server;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.util.HostUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ua.lab8.model.File;
import ua.lab8.model.Folder;

import javax.jms.*;
import java.sql.Timestamp;

public class AMQServer implements MessageListener {
    public static final String BROKER_URL = "tcp://localhost:61616";

    /**
     * Objects used to connect to ActiveMQ
     */
    private final Session session;
    private final Connection connection;
    private final MessageProducer replyProducer;
    private final MessageConsumer consumer;
    private volatile boolean didUserExit = false;

    /**
     * Objects used in creating the session
     */
    private final boolean transacted = false;
    private final int ackMode = Session.AUTO_ACKNOWLEDGE;

    /**
     * Queue name that this app will listen for messages from.
     */
    private final String QUEUE_NAME = "channel";
    private final DAO dao;

    public AMQServer(DAO dao) {
        this.dao = dao;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(transacted, ackMode);
            Destination adminQueue = session.createQueue(QUEUE_NAME);

            // set up the message producer to respond to messages from client.
            // We will get the destination to send to from the JMSReplyTo
            // header file from a message
            replyProducer = session.createProducer(null);
            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // set up a consumer to receive message off of the admin queue
            consumer = session.createConsumer(adminQueue);
            consumer.setMessageListener(this);
            System.out.println("Server is waiting for commands");
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage msg) {
            String receivedText;
            try {
                receivedText = msg.getText();
                System.out.println("Message received: " + receivedText);
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }

            String response = null;

            if (receivedText.startsWith("create folder"))
                response = dao.saveIfAbsent(
                        new Folder(receivedText.substring(14))
                ) ? "success" : "Folder \"" + receivedText.substring(14) + "\" is already present in file system";
            else if (receivedText.startsWith("create file"))
                response = createFile(receivedText.substring(12));
            else if (receivedText.startsWith("delete folder"))
                response = deleteFolder(receivedText.substring(14));
            else if (receivedText.startsWith("delete file"))
                response = deleteFile(receivedText.substring(12));
            else if (receivedText.startsWith("update file"))
                response = updateFile(receivedText.substring(12));
            else if (receivedText.startsWith("copy file"))
                response = copyFile(receivedText.substring(10));
            else if (receivedText.startsWith("folders"))
                response = dao.readAllFolders();
            else if (receivedText.startsWith("files"))
                response = dao.readAllFilesInFolder(
                        new Folder(receivedText.substring(6))
                );
            else if (receivedText.equals("exit")) {
                close();
                response = "";
                didUserExit = true;
            } else
                throw new IllegalArgumentException("Command \"" + receivedText + "\" is not recognized");

            try {
                if (!response.isEmpty()) replyProducer.send(message.getJMSReplyTo(), session.createTextMessage(response));
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }

        } else
            throw new RuntimeException("Message is not an instance of TextMessage");
    }

    private void close() {
        try {
            connection.close();
            session.close();
            replyProducer.close();
            consumer.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private String copyFile(String filePath) {
        String[] srcFileDst = filePath.split("/");
        dao.copyFile(srcFileDst[0], srcFileDst[1], srcFileDst[2]);
        return "";
    }

    private String updateFile(String updateInfo) {
        String[] pathAndUpdate = updateInfo.split("\\*");
        String[] path = pathAndUpdate[0].split("/");
        String[] update = pathAndUpdate[1].split(":");
        dao.updateFile(path[0], path[1], update[0], update[1]);
        return "";
    }

    private String deleteFile(String strPath) {
        var path = strPath.split("/");
        return dao.deleteFile(path[0], path[1]);
    }

    private String deleteFolder(String folderName) {
        dao.deleteFiles(folderName);
        return dao.deleteFolder(folderName);
    }

    private String createFile(String jsonFile) {
        JSONObject jo;
        try {
            jo = (JSONObject) new JSONParser().parse(jsonFile.substring(0, jsonFile.length() - 4));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        File file = new File(
                (String) jo.get("folder_name"),
                (String) jo.get("file_name"),
                Long.parseLong((String) jo.get("size")),
                Boolean.parseBoolean((String) jo.get("is_visible")),
                Boolean.parseBoolean((String) jo.get("is_readable")),
                Boolean.parseBoolean((String) jo.get("is_writeable")),
                Timestamp.valueOf((String) jo.get("last_updated")).toLocalDateTime()
        );
        return dao.saveIfAbsent(file) ? "success" :
                "File with name \"" + file.fileName + "\" is already present in folder \"" + file.folderName + "\"";
    }

    public boolean isDidUserExit() {
        return didUserExit;
    }
}
