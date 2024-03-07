import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Thread.sleep
import java.net.{DatagramPacket, DatagramSocket, ServerSocket, Socket}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.runtime.Arrays
import scala.util.Random
object Server {
  private val IDs = new mutable.LinkedHashMap[Int, Int]
  private val sockets = new mutable.LinkedHashMap[Int, (Socket, PrintWriter, BufferedReader)]
  val portNumber: Int = 12345
  private val TCPSocket = new ServerSocket(portNumber)

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
        println(clientSocket.getPort)
        System.out.println("client connected")
        new Thread() {
          override def run(): Unit = {
            clientService(clientSocket)
          }
        }.start()
      }
  }

  private def addClient(clientSocket: Socket, out: PrintWriter, in: BufferedReader): Unit = this.synchronized {
    val ID = 1000 + IDs.size
    IDs(clientSocket.getPort) = ID
    sockets(ID) = (clientSocket, out, in)
    out.println(ID.toString)
  }
  private def UDPClientService(): Unit ={
    var receiveBuffer: Array[Byte] = Array.fill[Byte](20)(0)
    var UDPSocket = new DatagramSocket(portNumber)
    while (true){
      receiveBuffer = Array.fill[Byte](2000)(0)
      val receivePacket: DatagramPacket = new DatagramPacket(receiveBuffer,2000)
      UDPSocket.receive(receivePacket)
      val msg = new String(receivePacket.getData)
      println("[SERVER]: received UDP message: " + msg.trim)
      sockets.keys.foreach((id:Int) => sockets(id)._2.println(msg))
      UDPSocket.close()
      UDPSocket = new DatagramSocket(portNumber)
      sleep(100)
    }
  }

  private def clientService(clientSocket: Socket): Unit = {
    val out: PrintWriter = new PrintWriter(clientSocket.getOutputStream, true)
    val in: BufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    while (true) {
      val msg: String = in.readLine()
      if msg == "GET OTHERS" then {
        val others = sockets.values.map((x:(Socket, PrintWriter, BufferedReader)) =>x._1).mkString(" ")
        out.print(others)
      }
      else if msg == "GET ID" then {
        addClient(clientSocket, out, in)
      }
      else {
        println("[SERVER]: received TCP message: " + msg.trim)
        val senderPort = clientSocket.getPort
        val ID = IDs(senderPort)
        sockets.keys.foreach((id:Int) => if id != ID then sockets(id)._2.println("["+ID.toString+"] "+msg))
      }
    }
  }
}