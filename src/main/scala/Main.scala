private object Main {
  def main(args: Array[String]): Unit ={
    if(args.length==0){
      new Thread() {
        override def run(): Unit = {
          Server.main()
        }
      }.start()
    }
    else{
      val address = args(0)
      new Thread() {
        override def run(): Unit = {
          Client(address).main()
        }
      }.start()
    }
  }

}
