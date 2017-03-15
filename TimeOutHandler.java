/**
 * @author      Nicolas Gonzalez
 * @version     1.0, 14 Mar 2017
 *
 */


import java.util.TimerTask;
import java.net.*;
import java.util.*;
import java.io.*;


public class TimeOutHandler extends TimerTask
{
    private FastClient fastClient; // reference to the FastClient
    private int sequenceNumber;   // the sequence number of the segment whose timeout we are considering

	public TimeOutHandler(FastClient c, int n)
	{
		fastClient = c;
        sequenceNumber = n;
	}

	public void run()
	{
            fastClient.processTime(sequenceNumber);
	}
}