package scbaz;

import scala.collection.mutable.Queue ;
import scala.xml.XML ;
import java.io.StringReader ;
import java.nio._ ;
import java.nio.channels._ ;

// a stream of messages across the network
//
// XXX The current protocol is not the desired one.
//     Currently, messages are separated in the socket's
//     data stream by prepending them with 4 bytes designating
//     their length in bytes.
//
// XXX when an badly formatted packet arrives, the stream should
// simply close the channel, not throw an exception.
class MessageStream(channel:SocketChannel) {
  // key...

  //  cookie..

  // waitingOnCookie

  val outQueue = new Queue[Message];
  val inQueue = new Queue[Message];
  
  var outBuf = ByteBuffer.allocate(0) ;

  var nextLength:Option[int] = None ;
  var inBuf = ByteBuffer.allocate(4) ;

  
  // Enqueue one message for sending.  This method might
  // or might not send processIO().
  def send(msg:Message) = {
    outQueue.enqueue(msg)
  }


  // Receive and return one message if any are available.
  // This method calls processIO before giving up if the
  // incoming queue is empty, so that callers can check
  // for new messages with just this one message send.
  def receive() : Option[Message] = {
    if(inQueue.isEmpty)
      processIO();

    if(inQueue.isEmpty)
      None
    else
      Some(inQueue.dequeue)
  }


  // Process as much outgoing data as possible without blocking.
  private def processOutput() = {
    var keepTrying = true;
    while(keepTrying) {
      keepTrying = false;  //turn it back on if any progress is made
      
      if(outBuf.remaining() > 0) {
        // a message packet is partially written.  write some more
	val written = channel.write(outBuf);
	if(written > 0) {
	  keepTrying = true;
	}
      }
      
      if((outBuf.remaining() == 0) && (! outQueue.isEmpty)) {
        // the last message packet is finished.  convert
        // the next message into a byte buffer for sending.
	val message = outQueue.dequeue;
	val node = message.toXML;
        val text = node.toString();
	val bytes = text.getBytes("UTF-8");
	
	outBuf = ByteBuffer.allocate(bytes.length + 4);
	outBuf.putInt(bytes.length);
	outBuf.put(bytes);
	outBuf.flip();
	
	keepTrying = true;
      }
    }
  }


  // Process as much incoming and outgoing data as possible without blocking.
  // Clients should poll this method periodically.
  def processIO():Unit = {
    // processInput: process as much incoming data as possible
    // without blocking.
    def processInput() = {
      def packetLength() = inBuf.getInt(0) ;

      var keepTrying = true;
      while(keepTrying) {
	keepTrying = false; // turn it back on if any progress is made

	nextLength match {
	  case None => {
	    // try to read in the next length
	    assert(inBuf.capacity() == 4);
	    if(inBuf.position() < 4) {
	      if(channel.read(inBuf) > 0)
		  keepTrying = true;
	    }
	    if(inBuf.position() == 4) {
	      inBuf.flip();
	      val length = inBuf.getInt();
	      nextLength = Some(length);
	      inBuf = ByteBuffer.allocate(length);
	    }
	  }

	  case Some(length) => {
	    // the length is known; try to read in the rest of the packet
	    assert(inBuf.capacity() == length);
	    if(inBuf.position() <= length) {
	      val length = channel.read(inBuf); 
	      if(length > 0) {
		keepTrying = true;
	      }
	    }
	    if(inBuf.position() == length) {
	      // got a complete packet!
	      keepTrying = true;

	      inBuf.flip();
	      val bytes = new Array[byte](length);
	      inBuf.get(bytes);
	      
	      val text = new String(bytes, "UTF-8");
	      val node = XML.load(new StringReader(text));
	      val message = Message.fromXML(node);
	      inQueue.enqueue(message);

	      inBuf = ByteBuffer.allocate(4);
	      nextLength = None;
	    }
	  }
	}
      }
    }

    processOutput();
    processInput();
  }

  def isConnected = channel.isConnected() ;

  // block until all output has been sent
  def flush() = { 
    while(! (outQueue.isEmpty && (outBuf.remaining() == 0))) {
      processOutput();
      Thread.sleep(10);
    }
  } 
}
