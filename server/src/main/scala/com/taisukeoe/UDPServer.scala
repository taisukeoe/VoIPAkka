package com.taisukeoe

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.taisukeoe.voip.{UDPEchoServerActor, UDPVoIPServerActor}

/**
  * Created by taisukeoe on 17/04/15.
  */
trait UDPServer {
  def serverActor(system: ActorSystem, inetSocketAddress: InetSocketAddress): ActorRef

  def main(args: Array[String]): Unit = {
    val Array(addr, port) = args
    val inetSocketAddress = new InetSocketAddress(addr, port.toInt)
    val system = ActorSystem("voip-server")
    serverActor(system,inetSocketAddress)
  }
}
