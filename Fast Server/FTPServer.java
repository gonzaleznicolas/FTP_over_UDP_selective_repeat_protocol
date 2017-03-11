import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class FTPServer extends Thread
{
  int windowSize = 1;
  float serverLoss = 0.0F;
  String serverName = "localhost";
  int serverPort = 8888;
  ServerSocket serverSocket = null;
  Socket socket = null;
  DatagramSocket udpSocket = null;
  String filename = null;
  DataInputStream in = null;
  DataOutputStream out = null;
  
  FileOutputStream fileWriter = null;
  File file = null;
  RxQueue queue = null;
  Random ran = null;
  



  int rcvBase;
  




  public FTPServer(int paramInt1, int paramInt2, float paramFloat)
  {
    serverPort = paramInt1;
    windowSize = paramInt2;
    serverLoss = paramFloat;
    rcvBase = 0;
  }
  





  public void receive()
  {
    try
    {
      for (;;)
      {
        byte[] arrayOfByte = new byte['Ϭ'];
        
        DatagramPacket localDatagramPacket = new DatagramPacket(arrayOfByte, arrayOfByte.length);
        udpSocket.receive(localDatagramPacket);
        
        if (ran.nextFloat() > serverLoss)
        {
          InetAddress localInetAddress = localDatagramPacket.getAddress();
          int i = localDatagramPacket.getPort();
          Segment localSegment1 = new Segment(localDatagramPacket);
          

          System.out.println("Received packet with sequence number: " + localSegment1.getSeqNum());
          Segment localSegment2; if ((localSegment1.getSeqNum() >= rcvBase) && (localSegment1.getSeqNum() < rcvBase + windowSize))
          {

            if (queue.getSegment(localSegment1.getSeqNum()) == null)
            {
              queue.add(localSegment1);
              queue.getNode(localSegment1.getSeqNum()).setStatus(1);
            }
            localSegment2 = new Segment(localSegment1.getSeqNum());
            localDatagramPacket = new DatagramPacket(localSegment2.getBytes(), localSegment2.getLength(), localInetAddress, i);
            udpSocket.send(localDatagramPacket);
            System.out.println("Packet within the receive window, sent ACK with sequence number: " + localSegment2.getSeqNum());


          }
          else if ((localSegment1.getSeqNum() >= rcvBase - windowSize) && (localSegment1.getSeqNum() < rcvBase))
          {

            localSegment2 = new Segment(localSegment1.getSeqNum());
            localDatagramPacket = new DatagramPacket(localSegment2.getBytes(), localSegment2.getLength(), localInetAddress, i);
            System.out.println("Packet within the previous receive window, sent ACK with sequence number: " + localSegment2.getSeqNum());
            
            udpSocket.send(localDatagramPacket);

          }
          else
          {
            System.out.println("Packet not within current or previous window, ignored and no ACK sent");
          }
          


          while ((queue.getHeadSegment() != null) && (queue.getHeadSegment().getSeqNum() == rcvBase))
          {
            localSegment2 = queue.getHeadSegment();
            fileWriter.write(localSegment2.getPayload());
            queue.remove();
            rcvBase += 1;
          }
        }
      }
    }
    catch (Exception localException1)
    {
      System.out.println("[Server] Exception while receiving file content: " + localException1.getMessage());
      System.out.println("[Server] Terminating connection and server");
      try {
        if (socket != null)
          socket.close();
        if (udpSocket != null) {
          udpSocket.close();
        }
      }
      catch (Exception localException2) {
        System.exit(0);
      }
      
      System.exit(0);
    }
  }
  













  public void waitForClient()
  {
    try
    {
      serverSocket = new ServerSocket(serverPort);
      socket = serverSocket.accept();
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());
      filename = in.readUTF();
      file = new File(filename);
      













      fileWriter = new FileOutputStream(file);
      queue = new RxQueue(windowSize);
      udpSocket = new DatagramSocket(serverPort);
      ran = new Random();
      TCPAck localTCPAck = new TCPAck(socket, in, out, fileWriter);
      localTCPAck.start();
      out.writeByte(0);
      System.out.printf("[Server] Ready to receive " + filename + " from client\n", new Object[0]);

    }
    catch (Exception localException1)
    {
      System.out.println("[Server] Exception during connection initialization: " + localException1.getMessage());
      System.out.println("[Server] Terminating connection and server");
      try {
        out.writeByte(1);
        if (socket != null)
          socket.close();
        if (udpSocket != null) {
          udpSocket.close();
        }
      }
      catch (Exception localException2) {
        System.exit(0);
      }
      

      System.exit(0);
    }
  }
  






  public static void main(String[] paramArrayOfString)
  {
    int i = 1;
    int j = 8888;
    float f = 0.0F;
    

    if (paramArrayOfString.length == 3)
    {
      j = Integer.parseInt(paramArrayOfString[0]);
      i = Integer.parseInt(paramArrayOfString[1]);
      f = Float.parseFloat(paramArrayOfString[2]);
    }
    else {
      System.out.println("[Server] wrong number of arguments, try again.");
      System.out.println("[Server] usage: java FTPServer serverport windowsize loss");
      System.exit(0);
    }
    

    FTPServer localFTPServer = new FTPServer(j, i, f);
    System.out.printf("[Server] Ready to receive client connection request\n", new Object[0]);
    localFTPServer.waitForClient();
    localFTPServer.receive();
  }
}
