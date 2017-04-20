package com.taisukeoe.voip

import akka.event.{Logging, LoggingAdapter, LoggingBus}

/**
  * Created by taisukeoe on 17/04/15.
  */
case class AndroidLogging(bus: LoggingBus, tag: String) extends LoggingAdapter {

  import Logging._

  def isErrorEnabled = bus.logLevel >= ErrorLevel

  def isWarningEnabled = bus.logLevel >= WarningLevel

  def isInfoEnabled = bus.logLevel >= InfoLevel

  def isDebugEnabled = bus.logLevel >= DebugLevel

  override protected def notifyInfo(message: String): Unit = android.util.Log.i(tag, message)

  override protected def notifyError(message: String): Unit = android.util.Log.e(tag, message)

  override protected def notifyError(cause: Throwable, message: String): Unit = android.util.Log.e(tag, message, cause)

  override protected def notifyWarning(message: String): Unit = android.util.Log.w(tag, message)

  override protected def notifyDebug(message: String): Unit = android.util.Log.d(tag, message)
}