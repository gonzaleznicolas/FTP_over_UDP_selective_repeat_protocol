/**
 * FastClient Class
 * FastClient implements a basic FTP application based on UDP data transmission and 
 * selective repeat rdt protocol
 * @author      Nicolas Gonzalez
 * @version     1.0, 12 Mar 2017
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * FastClient Class
 * 
 * FastClient implements a basic reliable FTP client application based on UDP data transmission and selective repeat protocol
 * 
 */
public class FastClient {

    private String serverName;
    private int serverPort;
    private int window;
    private int timeOut;
    byte[][] arrayOfChunks = {};

    private TxQueue senderQueue;
    private InetAddress serverIP;

    private String fileName;

    private Socket tcpSocketConnectingToServer;
    private DataInputStream tcpInputStreamFromServer;
    private DataOutputStream tcpOutputStreamToServer;

    public DatagramSocket udpSocketConnectingToServer;
    private Timer timer;

    public boolean allSegmentsAcked;


     /**
        * Constructor to initialize the program 
        * 
        * @param server_name    server name or IP
        * @param server_port    server port
        * @param file_name      file to be transfered
        * @param window         window size
    * @param timeout    time out value
        */
    public FastClient(String server_name, int server_port, int window, int timeout) {
        /* initialize */    
        serverName = server_name;
        serverPort = server_port;
        window = window;
        timeOut = timeout;

        senderQueue = new TxQueue(window);
        allSegmentsAcked = false;
    }
    
    /* send file */
    public void send(String file_name)
    {
        this.fileName = file_name;
        
        try
        {

            // INITIALIZE TCP STREAMS AND SOCKET
            tcpSocketConnectingToServer = new Socket(serverName, serverPort);
            tcpInputStreamFromServer = new DataInputStream(tcpSocketConnectingToServer.getInputStream());
            tcpOutputStreamToServer = new DataOutputStream(tcpSocketConnectingToServer.getOutputStream());

            // INITIAL HANDSHAKE OVER TCP
            tcpOutputStreamToServer.writeUTF(fileName);
            System.out.println("hi");
            byte response = tcpInputStreamFromServer.readByte();
            System.out.println("hello");
            if (response != (byte) 0)
            {
                System.out.println("An error occured establishing the TCP connection with the server");
                System.exit(0);
            }

            // if here, the initial handshake was successful


            // make an array of arrays of bytes. these arrays will be filled with the bytes from the file.
            // each array will be of size MAX_PAYLOAD_SIZE except for the last one which may be smaller.
            FileInputStream fin = null;
            File f = null;
            boolean bytesLeftToRead = true;
            //byte[][] arrayOfChunks = {};
            byte[] dataFromFile = new byte[Segment.MAX_PAYLOAD_SIZE];
            byte[] temp1; // a temporary array
            try
            {
                fin = new FileInputStream(fileName);
                f = new File(fileName);
                long fileSize = f.length();
                int numOfChunks = (int) (fileSize/((int) Segment.MAX_PAYLOAD_SIZE)) + 1;
                arrayOfChunks = new byte[numOfChunks][];
                int ithChunk = 0;
                while(bytesLeftToRead)
                {
                    for(int i = 0; i < dataFromFile.length; i++)
                    {
                        int byteReadAsInt = fin.read();
                        if (byteReadAsInt == -1)
                        {
                            // if we fell in here, the file had fewer than MAX_PAYLOAD_SIZE bytes left to read
                            // so make dataFromFIle byte array just the size needed for the number of bytes we read
                            temp1 = new byte[i];
                            System.arraycopy(dataFromFile, 0, temp1, 0, i);
                            dataFromFile = temp1;
                            bytesLeftToRead = false;
                            break; // break out of for loop
                        }
                        // if here, we read a byte from the file
                        dataFromFile[i] = (byte) byteReadAsInt;
                    }
                    arrayOfChunks[ithChunk] = dataFromFile.clone();
                    ithChunk++;
                }

                //System.out.println(Arrays.deepToString(arrayOfChunks));

            }
            catch (Exception e)
            {
                System.out.println("Exception reading file: " + e.getMessage());
                System.exit(0); // exit
            }
            finally
            {
                try
                {
                    if (fin != null)
                    {
                        fin.close();
                    }
                }
                catch (IOException ex)
                {
                    System.out.println("error closing file.");
                    System.exit(0); // exit
                }
            }
            if (arrayOfChunks[arrayOfChunks.length-1].length == 0)
                arrayOfChunks = Arrays.copyOf(arrayOfChunks, arrayOfChunks.length-1);
            //System.out.println(Arrays.deepToString(arrayOfChunks));


            // at this point, arrayOfChunks is an array of arrays of bytes. all of those bytes together are the file.
            // all of the arrays are MAX_PAYLOAD_SIZE in length, except for the last one which may be smaller.



            // TRANSFER ALL THE CHUNKS OVER UDP USING selective repeat protocol

            if (arrayOfChunks.length == 0) {allSegmentsAcked = true;} // if the file is empty, we are done


            // set up UDP socket
            serverIP = InetAddress.getByName(serverName);
            udpSocketConnectingToServer = new DatagramSocket();

            // set up timer
            timer = new Timer(true);  // make the thread daemon

            // start Ack receiving thread AckReceiver
            AckReceiver ackReceivingThread = new AckReceiver(this);  // give this thread this FastClient object so it can access the methods
            ackReceivingThread.start();

            // send packets. the main thread (this one) only has to send each segement once. if there is loss, the time out handler will take care
            // of retransmitting the segment. we know we have arrayOfChunks.length segments to send
            for (int i = 0; i< arrayOfChunks.length; i++)
            {

            }



















        }
        catch (Exception e)
        {
            System.out.println("An fatal error has occured. The program will exit.");
            System.exit(0);
        }



    }





    public synchronized void processSend(Segment segment)
    {
        // add segment to the queue, send segment,
        // set the state of segment in the queue as "sent" and
        // schedule timer task for the segment

        int sequenceNumber = segment.getSeqNum();

        // add segment to the queue
        try
        {
            senderQueue.add(segment);            
        }
        catch (InterruptedException e)
        {
            System.out.println("The method sending a segment was interrupted. The program will exit.");
            System.exit(0);
        }

        // send the segment
        DatagramPacket pktToSend = new DatagramPacket(segment.getBytes(), segment.getBytes().length, serverIP, serverPort);
        try{ udpSocketConnectingToServer.send(pktToSend);}
        catch (IOException e){ System.out.println("There was an excepion sending the packet."); System.exit(0);}

        // set the state of the node to SENT
        TxQueueNode node = senderQueue.getNode(sequenceNumber);
        if (node == null)
        {
            // if null was returned it means Segment with sequence number sequenceNumber is not in the queue/window. this is unexpected
            System.out.println("An unexpected error occured. The program will exit.");
            System.exit(0);
        }
        else // node holds the node into which the segment we just sent was put
        {
            // set the state to SENT
            node.setStatus(TxQueueNode.SENT);
        }

        // schedule timer task for this segment
        TimeOutHandler toh = new TimeOutHandler(this, sequenceNumber);
        timer.schedule(toh, timeOut);
    }


    public synchronized void processAck(Segment ack)
    {
        // If ack belongs to the current sender window => set the
        // state of segment in the transmission queue as
        // "acknowledged". Also, until an unacknowledged
        // segment is found at the head of the transmission
        // queue, keep removing segments from the queue
        // Otherwise => ignore ack

        // check if the ack received is acking a segment in the current sender window
        int ackNum = ack.getSeqNum();                         // the sequence number of the ack received
        TxQueueNode test = senderQueue.getNode(ackNum);       // if the packet for that sequence number is not in the queue, null is returned

        if (test == null)                // i.e. if the ack we received is not for a segment in the sender queue
            return;                      // ignore ack

        // if here, the ack received is for a segment which is in the queue and the node which holds that segment is held at the variable test

        test.setStatus(TxQueueNode.ACKNOWLEDGED);         // Set the state of the segment in the queue as acknowledged

        try
        {
            while(senderQueue.getHeadNode() != null && senderQueue.getHeadNode().getStatus() == TxQueueNode.ACKNOWLEDGED) // while the next segment at the
            {                                                                                                             // head is acknowledged
                senderQueue.remove();     // remove it
            }
        }
        catch(InterruptedException e)
        {
            System.out.println("InterruptedException happened. Program will exit.");
            System.exit(0);
        }


    }


    public synchronized void processTime(int seqNum)
    {
        // this method will be called when a timer expires. the sequence number of the segment
        // whose timer expired is seqNum. check whether the
        // time-out happened for a segment that belongs
        // to the current window and not yet acknowledged.
        // If yes => then resend the segment and schedule
        // timer task for the segment.
        // Otherwise => ignore the time-out event.

        TxQueueNode test = senderQueue.getNode(seqNum);
        if (test == null)
            return;  // if null was returned it means Segment with sequence number seqNum is not in the queue/window. so ignore the timeout event
        else if (test.getStatus() == TxQueueNode.ACKNOWLEDGED)
            return; // if the timeout that happened is for a segment which has already been acknowledged,ignore the timeout
        else if (test.getStatus() == TxQueueNode.SENT)      // if the timeout happened for a Segment in the current window and it has not yet been acked
        {

            // resend the segment #seqNum
            Segment segToSend = new Segment(seqNum, arrayOfChunks[seqNum]);
            DatagramPacket pktToSend = new DatagramPacket(segToSend.getBytes(), segToSend.getBytes().length, serverIP, serverPort);
            try{ udpSocketConnectingToServer.send(pktToSend);}
            catch (IOException e){ System.out.println("There was an excepion sending the packet."); System.exit(0);}

            // schedule a timer task for the segment
            TimeOutHandler toh = new TimeOutHandler(this, seqNum);
            timer.schedule(toh, timeOut);
        }
    }





    /**
     * A simple test driver
     * 
     */
    public static void main(String[] args) {
        int window = 10; //segments
        int timeout = 100; // milli-seconds (don't change this value)
        
        String server = "localhost";
        String file_name = "";
        int server_port = 0;
        
        // check for command line arguments
        if (args.length == 4) {
            // either provide 3 parameters
            server = args[0];
            server_port = Integer.parseInt(args[1]);
            file_name = args[2];
            window = Integer.parseInt(args[3]);
        }
        else {
            System.out.println("wrong number of arguments, try again.");
            System.out.println("usage: java FTPClient server port file windowsize");
            System.exit(0);
        }

        
        FastClient ftp = new FastClient(server, server_port, window, timeout);
        
        System.out.printf("sending file \'%s\' to server...\n", file_name);
        ftp.send(file_name);
        System.out.println("file transfer completed.");
    }

}
