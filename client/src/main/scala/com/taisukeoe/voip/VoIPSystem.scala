package com.taisukeoe.voip

import java.net.InetSocketAddress

import akka.actor._

/**
  * Created by taisukeoe on 17/04/15.
  */
case class VoIPSystem(name: String, listener: NetworkEventListener) {
  private val sys = ActorSystem(name)
  private val playActor = sys.actorOf(Props(classOf[PlayActor]), "play")
  private val listenerActor = sys.actorOf(NetworkEventListenerActor.props(listener), "listener")
  private val socketActor = sys.actorOf(UDPClientActor.props(playActor, listenerActor), "socket")
  private var recordActor: Option[ActorRef] = None

  private def createRecordActor = sys.actorOf(Props(classOf[RecordActor], socketActor))

  def activateRecord(): Unit = if (isAvailable) {
    if (recordActor.isEmpty)
      recordActor = Option(createRecordActor)
    recordActor.foreach(_ ! Record)
  }

  def deactivateRecord(): Unit = if (isAvailable) {
    recordActor.foreach(_ ! PoisonPill)
    recordActor = None
  }

  def connect(inetSocketAddress: InetSocketAddress) = if (isAvailable) socketActor ! ConnectTo(inetSocketAddress)

  def release(): Unit = {
    sys.shutdown()
    recordActor = None
  }

  def isAvailable: Boolean = !sys.isTerminated
}

