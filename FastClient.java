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

    private TxQueue senderQueue;

    private String fileName;

    private Socket tcpSocketConnectingToServer;
    private DataInputStream tcpInputStreamFromServer;
    private DataOutputStream tcpOutputStreamToServer;


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
            byte[][] arrayOfChunks = {};
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






















        }
        catch (Exception e)
        {
            System.out.println("An fatal error has occured. The program will exit.");
            System.exit(0);
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
