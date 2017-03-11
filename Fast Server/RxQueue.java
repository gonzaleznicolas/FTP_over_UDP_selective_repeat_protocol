import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
























public class RxQueue
{
  private final ReentrantLock mutex;
  private final Condition notFull;
  private final Condition notEmpty;
  private RxQueueNode head = null;
  private RxQueueNode tail = null;
  private int count = 0;
  private int length = 0;
  






  public RxQueue(int paramInt)
  {
    mutex = new ReentrantLock();
    
    notFull = mutex.newCondition();
    notEmpty = mutex.newCondition();
    
    length = paramInt;
  }
  







  public RxQueueNode getNode(int paramInt)
  {
    mutex.lock();
    try
    {
      Object localObject1 = null;
      RxQueueNode localRxQueueNode = head;
      for (int i = 0; i < count; i++) {
        if (seg.getSeqNum() == paramInt)
        {
          localObject1 = localRxQueueNode;
          break;
        }
        

        localRxQueueNode = next;
      }
      

      return localObject1;
    }
    finally
    {
      mutex.unlock();
    }
  }
  







  public Segment getSegment(int paramInt)
  {
    mutex.lock();
    try
    {
      Object localObject1 = null;
      RxQueueNode localRxQueueNode = head;
      for (int i = 0; i < count; i++) {
        if (seg.getSeqNum() == paramInt)
        {
          localObject1 = localRxQueueNode;
          break;
        }
        

        localRxQueueNode = next;
      }
      
      Segment localSegment;
      if (localObject1 != null) {
        return seg;
      }
      return null;
    }
    finally
    {
      mutex.unlock();
    }
  }
  






  public RxQueueNode getHeadNode()
  {
    mutex.lock();
    try
    {
      RxQueueNode localRxQueueNode1 = null;
      if (count != 0) {
        localRxQueueNode1 = head;
      }
      return localRxQueueNode1;
    }
    finally
    {
      mutex.unlock();
    }
  }
  






  public Segment getHeadSegment()
  {
    mutex.lock();
    try
    {
      Segment localSegment1 = null;
      if (count != 0) {
        localSegment1 = head.seg;
      }
      return localSegment1;
    }
    finally
    {
      mutex.unlock();
    }
  }
  







  public void add(Segment paramSegment)
    throws InterruptedException
  {
    mutex.lock();
    
    try
    {
      if (count == length) {
        notFull.await();
      }
      RxQueueNode localRxQueueNode1;
      if (count == 0)
      {
        localRxQueueNode1 = new RxQueueNode(paramSegment);
        head = localRxQueueNode1;
        tail = localRxQueueNode1;
        head.next = null;
        tail.next = null;
      }
      else
      {
        localRxQueueNode1 = new RxQueueNode(paramSegment);
        RxQueueNode localRxQueueNode2 = head;
        RxQueueNode localRxQueueNode3 = localRxQueueNode2;
        int i = 0;
        while (localRxQueueNode2 != null)
        {
          if (seg.getSeqNum() > seg.getSeqNum())
          {
            if (head == localRxQueueNode2) {
              head = localRxQueueNode1;
            } else
              next = localRxQueueNode1;
            next = localRxQueueNode2;
            i = 1;
            break;
          }
          

          localRxQueueNode3 = localRxQueueNode2;
          localRxQueueNode2 = next;
        }
        
        if (i == 0)
        {
          next = localRxQueueNode1;
          next = null;
          tail = localRxQueueNode1;
        }
      }
      
      count += 1;
      

      notEmpty.signal();
    }
    finally
    {
      mutex.unlock();
    }
  }
  







  public Segment remove()
    throws InterruptedException
  {
    mutex.lock();
    

    try
    {
      if (count == 0) {
        notEmpty.await();
      }
      
      RxQueueNode localRxQueueNode = head;
      if (head == tail)
      {
        head = null;
        tail = null;
      }
      else
      {
        head = head.next;
      }
      count -= 1;
      

      notFull.signal();
      
      return seg;
    }
    finally
    {
      mutex.unlock();
    }
  }
  





  public int size()
  {
    mutex.lock();
    
    try
    {
      return count;
    }
    finally
    {
      mutex.unlock();
    }
  }
  






  public boolean isEmpty()
  {
    mutex.lock();
    try
    {
      return count == 0;
    }
    finally
    {
      mutex.unlock();
    }
  }
  






  public boolean isFull()
  {
    mutex.lock();
    try
    {
      return count == length;
    }
    finally
    {
      mutex.unlock();
    }
  }
}
