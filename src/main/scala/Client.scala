import java.io.{BufferedReader, IOException, InputStreamReader, PrintWriter}
import java.lang.Runnable
import java.lang.Thread.sleep
import java.net.{DatagramPacket, DatagramSocket, InetAddress, Socket, SocketException}
import scala.util.Random
class TCPClient(val socket: Socket, val ID : Int, in: BufferedReader, out: PrintWriter) {
  val UDPSocket = new DatagramSocket(socket.getLocalPort)
  def main(): Unit = {
    new Thread(){
      override def run(): Unit = {
        TCPConnection()
      }
      }.start()
    new Thread() {
      override def run(): Unit = {
        try{
          while(true){
            UDPConnection()
            sleep(400)
          }
        } catch {
          case _: SocketException => 
        }

      }
    }.start()
  }

  def UDPConnection(): Unit = {
        val address: InetAddress = InetAddress.getByName("localhost")
        val sendBuffer: Array[Byte] = ("Ping Scala UDP " + String.valueOf(ID)).getBytes()
        val sendPacket: DatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, 12345)
        UDPSocket.send(sendPacket)

  }
  def TCPConnection(): Unit ={
    println("SCALA TCP CLIENT")
    while (true) {
      out.println("Ping Scala TCP")
      sleep(400)
      val response: String = in.readLine()
      System.out.println("[" + ID.toString + "]: received response: " + response)
    }
  }
  def close(): Unit ={
    socket.close()
  }
}
object TCPClient{
  def apply(): TCPClient ={
    val hostName: String = "localhost"
    try{
      val socket = new Socket(hostName, 12345)

      // in & out streams
      val out: PrintWriter = new PrintWriter(socket.getOutputStream, true)
      val in: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      out.println("GetID")
      val ID: Int = in.readLine().toInt
      new TCPClient(socket, ID, in, out)
    }
    catch{
      case _: IOException => apply()
    }
  }
}