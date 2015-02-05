package com.jakemadethis.util


object Logger {
  case class Error(msg : String) {
    override def toString = msg
  }
  case class Verbose(msg : String) {
    override def toString = msg
  }
}

class Logger {
  type P = PartialFunction[Any, Unit]
  
  def !(message : Any) = receiveIfDefined(message)
  def log(message : String, data : Product) = receiveIfDefined(message.format(data.productIterator.toSeq : _*) -> data)
  def log(data : Product) = receiveIfDefined(data)
  def logfmt[T](message : String, data : T)(f : (String, T) => String) = receiveIfDefined(f(message,data) -> data)
  def receiveIfDefined(message : Any) = if (receive.isDefinedAt(message)) receive(message)
  
  def @!(msg : String) = this ! Logger.Error(msg)
  
  def receive : PartialFunction[Any, Unit] = {
    case Logger.Error(msg) => println(msg)
  }
}
