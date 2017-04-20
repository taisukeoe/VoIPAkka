package com.taisukeoe.voip

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{Udp, UdpConnected}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

import scala.concurrent.duration._

/**
  * Created by taisukeoe on 17/04/19.
  */
class UDPEchoServerTest extends TestKit(ActorSystem("VoIP-Spec")) with FlatSpecLike with ImplicitSender with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "UDPEchoServer" must "return byte array as it is" in {
    //Set up UDP Echo Server
    val playActor: TestProbe = TestProbe()
    val listenerActor: TestProbe = TestProbe()
    val udpClient: ActorRef = system.actorOf(UDPClientActor.props(playActor.ref, listenerActor.ref))

    val inetSocketAddress: InetSocketAddress = new InetSocketAddress("localhost", 1234)
    val server = system.actorOf(Props(classOf[UDPEchoServerActor], inetSocketAddress))

    //Set up UDP Client
    udpClient ! ConnectTo(inetSocketAddress)
    listenerActor.expectMsg(5 seconds, NetworkEvent.Connected(inetSocketAddress))

    //Send a packet and receive it back
    val arrayByte = Array(0.toByte, 1.toByte)
    udpClient ! ByteString(arrayByte)
    expectMsg(5 second, Ack)
    playActor.expectMsg(5 seconds, AudioPacketMessage(ByteString(arrayByte)))

    udpClient ! UdpConnected.Disconnect
    listenerActor.expectMsg(5 seconds, NetworkEvent.Disconnected(inetSocketAddress))

    server ! Udp.Unbind
    system.stop(server)
  }
}