This is a file transfer application.
The file transfer happens over UDP. Reliable data transfer is ensured by the selective repeat protocol.

Run the FastServer and the FastClient in different directories. The file to transfer should be in the same directory as the FastClient. The transferred file will be placed in the same directory as the FastServer.

Give both the FastClient and the FastServer the same window size.

Run the server before running the client.

The <loss> argument to the server is the proportion of packets which will be dropped. This is there to emphasize the fact that reliable data transfer is implemented at the application layer. You could select 0.9 for <loss> i.e. 90% of packets are dropped by the server, and the file transfer would still happen and be reliable.

Client Usage:
java FastClient <server-ip> <server-port> <file-name> <window-size>

java -jar FastServer.jar <server-port> <window-size> <loss>
