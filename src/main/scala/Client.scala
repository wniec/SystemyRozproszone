import java.io.{BufferedReader, IOException, InputStreamReader, PrintWriter}
import java.lang.Runnable
import java.lang.Thread.sleep
import java.net.{DatagramPacket, DatagramSocket, InetAddress, MulticastSocket, Socket, SocketException}
import java.util.Scanner
import scala.collection.immutable.Stream.Empty.++
import scala.util.Random
private class Client(val socket: Socket, val ID : Int, in: BufferedReader, out: PrintWriter) {
  var UDPSocket = new DatagramSocket(socket.getLocalPort)
  private var isTyping: Boolean = false
  private var multicastSocket: Option[DatagramSocket] = None
  private val group: InetAddress = InetAddress.getByName("230.0.0.0")
  private val art: String =
    s"""
       |_____w
       |                   .d88888888bo.
       |                 .d8888888888888b.
       |                 8888888888888888b
       |                 888888888888888888
       |                 888888888888888888
       |                  Y8888888888888888
       |            ,od888888888888888888P
       |         .'`Y8P'```'Y8888888888P'
       |       .'_   `  _     'Y88888888b
       |      /  _`    _ `      Y88888888b   ____
       |   _  | /  \\  /  \\      8888888888.d888888b.
       |  d8b | | /|  | /|      8888888888d8888888888b
       | 8888_\\ \\_|/  \\_|/      d888888888888888888888b
       | .Y8P  `'-.            d88888888888888888888888
       |/          `          `      `Y8888888888888888
       ||                        __    888888888888888P
       | \\                       / `   dPY8888888888P'
       |  '._                  .'     .'  `Y888888P`
       |     `"'-.,__    ___.-'    .-'
       |    jgs  `-._````  __..--'`
       |             ``````
       |""".stripMargin
  val art1: String =
  s"""
     |.    _    +     .  ______   .          .
     |  (      /|\\      .    |      \\      .   +
     |      . |||||     _    | |   | | ||         .
     | .      |||||    | |  _| | | | |_||    .
     |    /\\  ||||| .  | | |   | |      |       .
     | __||||_|||||____| |_|_____________\\__________
     | . |||| |||||  /\\   _____      _____  .   .
     |   |||| ||||| ||||   .   .  .         ________
     |  . \\|`-'|||| ||||    __________       .    .
     |     \\__ |||| ||||      .          .     .
     |  __    ||||`-'|||  .       .    __________
     | .    . |||| ___/  ___________             .
     |    . _ ||||| . _               .   _________
     | _   ___|||||__  _ \\\\--//    .          _
     |      _ `---'    .)=\\oo|=(.   _   .   .    .
     | _  ^      .  -    . \\.|
     |""".stripMargin

  def main(): Unit = {
    new Thread(){
      override def run(): Unit = {
        TCPConnection()
      }
      }.start()
    new Thread() {
      override def run(): Unit = {
          while(true){
            UDPConnection()
        }
      }
    }.start()
    new Thread() {
      override def run(): Unit = {
          while (true) {
            multicastListen()
            sleep(20)
          }

      }
    }.start()
  }

  private def UDPConnection(): Unit = {
    sleep(400)
    if(!isTyping){
      val sign: Int = System.in.read()
      if sign == 117 then {
        val address: InetAddress = InetAddress.getByName("localhost")
        isTyping = true
        sendMessage(UDPSocket,address,12345)
      }
      else if sign == 109 then {
        multicastSocket = Some(new DatagramSocket())
        val address: InetAddress = InetAddress.getByName("230.0.0.0")
        isTyping = true
        sendMessage(multicastSocket.get,address,22223)
      }
    }
  }
  private def getInputLines: String ={
    val userInput: Scanner = new Scanner(System.in)
    var input = userInput.nextLine
    while (input.isEmpty) {
      input = userInput.nextLine()
    }
    var text = input
    while (input.nonEmpty) {
      input = userInput.nextLine()
      text = text + "\n" + input
    }
    text
  }
  private def sendMessage(sck: DatagramSocket, addr: InetAddress, portNumber: Int): Unit ={
    val lines = getInputLines
    val sendBuffer: Array[Byte] = lines.getBytes()
    val sendPacket: DatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, addr, portNumber)
    sck.send(sendPacket)
    isTyping = false
  }
  private def multicastListen(): Unit = this.synchronized{
    val receiveBuffer: Array[Byte] = Array.fill[Byte](2000)(0)
    val receivePacket: DatagramPacket = new DatagramPacket(receiveBuffer, 2000)
    val multicastReceiveSocket = new MulticastSocket(22223)
    import java.net.InetAddress
    multicastReceiveSocket.joinGroup(group)
    sleep(10)
    multicastReceiveSocket.receive(receivePacket)
    val msg = new String(receivePacket.getData)
    if(msg != null){
      println("[" + ID.toString + "]: received response: " + msg.trim)

    }
    multicastReceiveSocket.disconnect()
    multicastReceiveSocket
  }
  private def TCPConnection(): Unit ={
    println("SCALA TCP CLIENT")
    while (true) {
      sleep(600)
      if(!isTyping){
        out.println("Ping Scala TCP")
        var response: String = in.readLine()
        if response.nonEmpty then {
          sleep(200)
          System.out.println("[" + ID.toString + "]: received response: " + response.trim)
          while (response != null && response.nonEmpty && in.ready()) {
            response = in.readLine()
            System.out.println(response.trim())
          }
        }
      }
    }
  }
  def close(): Unit ={
    socket.close()
  }
}
object Client{
  def apply(hostname: String): Client ={
    try{
      val socket = new Socket(InetAddress.getByName(hostname), 12345)
      val out: PrintWriter = new PrintWriter(socket.getOutputStream, true)
      val in: BufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      out.println("GET ID")
      val ID: Int = in.readLine().toInt
      new Client(socket, ID, in, out)
    }
    catch{
      case _: IOException => apply(hostname)
    }
  }
}