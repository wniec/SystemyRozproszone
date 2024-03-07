import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.{DatagramPacket, DatagramSocket, ServerSocket, Socket}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.runtime.Arrays
import scala.util.Random
object TCPServer {
  val IDs = new mutable.LinkedHashMap[Int, Int]
  val sockets = new mutable.LinkedHashMap[Int, (Socket, PrintWriter, BufferedReader)]
  val portNumber: Int = 12345
  val TCPSocket = new ServerSocket(portNumber)


  def main(): Unit = {
    println("SCALA UDP SERVER")
    new Thread(){
      override def run(): Unit = {
        UDPClientService()
      }
    }.start()
    println("SCALA TCP SERVER")

      while(true){
        val clientSocket: Socket = TCPSocket.accept()
        System.out.println("client connected")
        new Thread() {
          override def run(): Unit = {
            clientService(clientSocket)
          }
        }.start()
      }
  }

  def addClient(clientSocket: Socket, out: PrintWriter, in: BufferedReader): Unit = this.synchronized {
    val ID = 1000 + IDs.size
    //println(ID)
    IDs(clientSocket.getPort) = ID
    //println(IDs)
    sockets(ID) = (clientSocket, out, in)
    out.println(ID.toString)
  }
  def UDPClientService(): Unit ={
    var receiveBuffer: Array[Byte] = Array.fill[Byte](20)(0)
    var i = 0;
    while (true){
      val UDPSocket = new DatagramSocket(portNumber)
      receiveBuffer = Array.fill[Byte](2000)(0)
      val receivePacket: DatagramPacket = new DatagramPacket(receiveBuffer,2000)
      UDPSocket.receive(receivePacket)
      val msg = new String(receivePacket.getData)
      println("[SERVER]: received UDP message: " + msg.trim)
      UDPSocket.close()
    }
  }

  def clientService(clientSocket: Socket): Unit = {
    // in & out streams
    val out: PrintWriter = new PrintWriter(clientSocket.getOutputStream, true)
    val in: BufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    var i = 0
    while (true) {
      // read msg, send response
      val msg: String = in.readLine()


      if msg == "GetID" then {

        addClient(clientSocket,out,in)

      } else {
        println("[SERVER]: received TCP message: " + msg.trim)
        //println(i)
        i+=1
        //println(IDs)
        //println(sockets)
        val senderPort = clientSocket.getPort
        val ID = IDs(senderPort)
        sockets.keys.foreach((id:Int) => if id != ID then sockets(id)._2.println("["+ID.toString+"] "+msg))
        //out.println("Pong Scala TCP")
      }
    }
  }
}