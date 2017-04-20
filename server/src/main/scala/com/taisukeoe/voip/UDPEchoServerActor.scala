package com.taisukeoe.voip

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Udp}
import com.taisukeoe.UDPServer

/**
  * Created by taisukeoe on 17/04/15.
  */
class UDPEchoServerActor(inetSocketAddress: InetSocketAddress) extends Actor with ActorLogging{
  import context.system
  IO(Udp) ! Udp.Bind(self,inetSocketAddress)

  def receive = {
    case Udp.Bound(local) =>
      log.debug(s"bound to $local")
      context.become(receiveAfterConnection(sender()))
  }

  def receiveAfterConnection(connection: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      log.debug(s"received $data via Udp")
      connection ! Udp.Send(data, remote)
    case Udp.Unbind  => connection ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}
object UDPEchoServer extends UDPServer{
  override def serverActor(system: ActorSystem, inetSocketAddress: InetSocketAddress): ActorRef = system.actorOf(Props(classOf[UDPEchoServerActor], inetSocketAddress),"udp-echo")
}