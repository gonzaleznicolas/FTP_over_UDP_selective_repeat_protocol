/**
 * @author      Nicolas Gonzalez
 * @version     1.0, 14 Mar 2017
 *
 */

import java.net.*;
import java.io.*;

public class AckReceiver extends Thread
{

    private FastClient fastClient;


    public AckReceiver(FastClient c)
    {
        fastClient = c;
    }

    public void run()
    {
        // allocate space for receiving an ack packet (4 bytes)
        byte[] receiveAck = new byte[4]; // the ack is only 4 bytes so i only need to allocate 4 bytes
        DatagramPacket ack = new DatagramPacket(receiveAck, receiveAck.length);

        while (fastClient.allSegmentsAcked == false)
        {
            try
            {
                // receive ack
                fastClient.udpSocketConnectingToServer.receive(ack);
                Segment ackReceived = new Segment(ack);

                fastClient.processAck(ackReceived);
            }
            catch (IOException e)
            {
                // do nothing. this exception will be thrown when the udp socket is closed. i.e. the file transfer is done
            }
        }
    }

}
