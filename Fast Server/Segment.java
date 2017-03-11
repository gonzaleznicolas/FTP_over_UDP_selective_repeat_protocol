import java.io.PrintStream;
import java.net.DatagramPacket;
import java.util.Arrays;






























public class Segment
{
  public static final int HEADER_SIZE = 4;
  public static final int MAX_PAYLOAD_SIZE = 1000;
  public static final int MAX_SEGMENT_SIZE = 1004;
  private int seqNum;
  private byte[] payload;
  
  public Segment()
  {
    this(0, new byte[0]);
  }
  







  public Segment(int paramInt)
  {
    this(paramInt, new byte[0]);
  }
  










  public Segment(int paramInt, byte[] paramArrayOfByte)
  {
    setSeqNum(paramInt);
    setPayload(paramArrayOfByte);
  }
  







  public Segment(Segment paramSegment)
  {
    this(seqNum, payload);
  }
  








  public Segment(byte[] paramArrayOfByte)
  {
    setBytes(paramArrayOfByte);
  }
  









  public Segment(DatagramPacket paramDatagramPacket)
  {
    this(Arrays.copyOf(paramDatagramPacket.getData(), paramDatagramPacket.getLength()));
  }
  




  public void setPayload(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length > 1000) {
      throw new IllegalArgumentException("Payload is too large");
    }
    
    payload = Arrays.copyOf(paramArrayOfByte, paramArrayOfByte.length);
  }
  



  public byte[] getPayload()
  {
    return payload;
  }
  




  public int getLength()
  {
    return payload.length + 4;
  }
  



  public int getSeqNum()
  {
    return seqNum;
  }
  



  public void setSeqNum(int paramInt)
  {
    if (paramInt < 0) {
      throw new IllegalArgumentException("Negative sequence number");
    }
    seqNum = paramInt;
  }
  





  public String toString()
  {
    return "Seq#" + seqNum + "\n" + Arrays.toString(payload);
  }
  







  public byte[] getBytes()
  {
    byte[] arrayOfByte = new byte[4 + payload.length];
    

    arrayOfByte[0] = ((byte)seqNum);
    arrayOfByte[1] = ((byte)(seqNum >>> 8));
    arrayOfByte[2] = ((byte)(seqNum >>> 16));
    arrayOfByte[3] = ((byte)(seqNum >>> 24));
    

    System.arraycopy(payload, 0, arrayOfByte, 4, payload.length);
    
    return arrayOfByte;
  }
  










  public void setBytes(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length < 4) {
      throw new IllegalArgumentException("Segment header missing");
    }
    
    if (paramArrayOfByte.length > 1004) {
      throw new IllegalArgumentException("Payload is too large");
    }
    
    int i = paramArrayOfByte[0] & 0xFF;
    int j = paramArrayOfByte[1] & 0xFF;
    int k = paramArrayOfByte[2] & 0xFF;
    int m = paramArrayOfByte[3] & 0xFF;
    seqNum = ((m << 24) + (k << 16) + (j << 8) + i);
    


    payload = new byte[paramArrayOfByte.length - 4];
    System.arraycopy(paramArrayOfByte, 4, payload, 0, payload.length);
  }
  





  public static void main(String[] paramArrayOfString)
  {
    byte[] arrayOfByte1 = new byte['Ϩ'];
    
    Arrays.fill(arrayOfByte1, (byte)0);
    arrayOfByte1[0] = 1;
    arrayOfByte1['ϧ'] = 1;
    

    Segment localSegment1 = new Segment(1, arrayOfByte1);
    

    System.out.println("seg1");
    System.out.println(localSegment1);
    System.out.println();
    

    Segment localSegment2 = new Segment();
    


    localSegment2.setBytes(localSegment1.getBytes());
    

    System.out.println("seg2");
    System.out.println(localSegment2);
    System.out.println();
    

    byte[] arrayOfByte2 = localSegment2.getBytes();
    DatagramPacket localDatagramPacket = new DatagramPacket(arrayOfByte2, arrayOfByte2.length);
    

    Segment localSegment3 = new Segment(localDatagramPacket);
    

    System.out.println("seg3");
    System.out.println(localSegment3);
    System.out.println();
  }
}
