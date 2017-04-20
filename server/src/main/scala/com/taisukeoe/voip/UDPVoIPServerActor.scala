package com.taisukeoe.voip

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Udp}
import com.taisukeoe.UDPServer

/**
  * Created by taisukeoe on 17/04/15.
  */
class UDPVoIPServerActor(inetSocketAddress: InetSocketAddress) extends Actor with ActorLogging {

  import context.system

  IO(Udp) ! Udp.Bind(self, inetSocketAddress)

  def receive = {
    case Udp.Bound(local) =>
      log.debug(s"bound to $local")
      context.become(receiveAfterConnection(sender(), Set.empty))
  }

  def receiveAfterConnection(connection: ActorRef, remoteSet: Set[InetSocketAddress]): Receive = {
    case Udp.Received(data, remote) =>
      val isNew = !remoteSet(remote)
//      log.debug(s"received $data via Udp from $remote. Is it new?:$isNew currentRemoteSet:$remoteSet")
      remoteSet.filterNot(_ == remote).foreach(r => connection ! Udp.Send(data, r))
      if (isNew)
        context become receiveAfterConnection(connection, remoteSet + remote)
    case Udp.Unbind => connection ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}
object UDPVoIPServer extends UDPServer{
  override def serverActor(system: ActorSystem, inetSocketAddress: InetSocketAddress): ActorRef = system.actorOf(Props(classOf[UDPVoIPServerActor], inetSocketAddress),"udp-voip")
}