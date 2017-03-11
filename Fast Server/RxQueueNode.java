










public class RxQueueNode
{
  private int segmentStatus = -1;
  
  public Segment seg = null;
  public RxQueueNode next = null;
  

  public static final int SENT = 0;
  

  public static final int ACKNOWLEDGED = 1;
  


  public RxQueueNode(Segment paramSegment)
  {
    seg = paramSegment;
  }
  





  public void setStatus(int paramInt)
  {
    segmentStatus = paramInt;
  }
  




  public int getStatus()
  {
    return segmentStatus;
  }
}
