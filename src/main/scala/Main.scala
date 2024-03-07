object Main {
  @main
  def main(): Unit ={
    val T1: Thread = new Thread(){
      override def run(): Unit ={
        (new TCPServer).main()
      }
    }
    val T2: Thread = new Thread() {
      override def run(): Unit = {
        TCPClient.main()
      }
    }
    T1.start()
    T2.start()
  }

}
