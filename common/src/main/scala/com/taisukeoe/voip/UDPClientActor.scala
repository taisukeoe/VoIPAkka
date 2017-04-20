package com.taisukeoe.voip

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Udp.Event
import akka.io.{IO, UdpConnected}
import akka.util.ByteString

/**
  * Created by taisukeoe on 17/04/15.
  */
object UDPClientActor {
  def props(playActor: ActorRef, listener: ActorRef) = Props(classOf[UDPClientActor], playActor, listener)
}

class UDPClientActor(playActor: ActorRef, listener: ActorRef) extends Actor with ActorLogging {

  import context.system

  override def receive: Receive = receiveBeforeConnection

  private def receiveBeforeConnection: Receive = {
    case ConnectTo(address) =>
      IO(UdpConnected) ! UdpConnected.Connect(self, address)

      context become receiveDuringConnecting(address)
  }

  private def receiveDuringConnecting(address: InetSocketAddress): Receive = {
    case c@UdpConnected.Connected =>
      listener ! NetworkEvent.Connected(address)
      context become receiveAfterConnection(sender, address)

    case c@(UdpConnected.Disconnected | UdpConnected.CommandFailed(_)) =>
      listener ! NetworkEvent.Disconnected(address)
      context unbecome()
  }

  private def receiveAfterConnection(connection: ActorRef, address: InetSocketAddress): Receive = {
    case content: ByteString =>
      connection ! UdpConnected.Send(content)
      sender() ! Ack

    case UdpConnected.Received(content) =>
      playActor ! AudioPacketMessage(content)

    case UdpConnected.CommandFailed(command) =>
      listener ! NetworkEvent.Error(address)

    case UdpConnected.Disconnect =>
      listener ! NetworkEvent.Disconnected(address)
      context become receiveBeforeConnection
  }
}
case object Ack extends Event

case class AudioPacketMessage(content: ByteString)

case class ConnectTo(address: InetSocketAddress)

sealed trait NetworkEvent extends Serializable

object NetworkEvent {

  case class Connected(address: InetSocketAddress) extends NetworkEvent

  case class Disconnected(address: InetSocketAddress) extends NetworkEvent

  case object ConnectionFailed extends NetworkEvent

  case class Error(address: InetSocketAddress) extends NetworkEvent

  lazy val ACTION = "network_event"
  lazy val eventKey = "network_event_key"
}

trait NetworkEventListener {
  def onEvent(networkEvent: NetworkEvent)
}

object NetworkEventListenerActor {
  def props(listener: NetworkEventListener) = Props(classOf[NetworkEventListenerActor], listener)
}

class NetworkEventListenerActor(listener: NetworkEventListener) extends Actor {
  override def receive: Receive = {
    case ev: NetworkEvent => listener.onEvent(ev)
  }
}

