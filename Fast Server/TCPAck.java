import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.Socket;




public class TCPAck
  extends Thread
{
  Socket clientSocket = null;
  DataInputStream clientInputStream = null;
  DataOutputStream clientOutputStream = null;
  FileOutputStream clientFileWriter = null;
  









  public TCPAck(Socket paramSocket, DataInputStream paramDataInputStream, DataOutputStream paramDataOutputStream, FileOutputStream paramFileOutputStream)
  {
    clientSocket = paramSocket;
    clientInputStream = paramDataInputStream;
    clientOutputStream = paramDataOutputStream;
    clientFileWriter = paramFileOutputStream;
  }
  
  public void run()
  {
    try {
      if (clientInputStream.readByte() == 0)
      {
        System.out.println("[Server] file transfer completed");
        System.out.println("[Server] Terminating connection and server");
        clientSocket.close();
        clientFileWriter.close();
        System.exit(0);

      }
      else
      {
        System.out.println("[Server] Invalid termination message from client");
        System.out.println("[Server] Terminating connection and server");
        clientSocket.close();
        clientFileWriter.close();
        System.exit(0);
      }
    }
    catch (Exception localException1)
    {
      System.out.println("[Server] Closing Exception (TCP connection prematurely closed by client): " + localException1.getMessage());
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      }
      catch (Exception localException2) {
        System.exit(0);
      }
      
      System.exit(0);
    }
  }
}
