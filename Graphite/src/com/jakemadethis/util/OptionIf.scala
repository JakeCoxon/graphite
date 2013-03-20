package com.jakemadethis.util

object OptionIf {
  implicit def toOptionIf[T](any : T) = new OptionIf[T](any)
}
class OptionIf[T](any : T) {
  def optionIf(pred : (T) => Boolean) = if (pred(any)) Option(any) else None
}
