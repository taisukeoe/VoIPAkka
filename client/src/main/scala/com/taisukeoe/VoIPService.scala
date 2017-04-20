package com.taisukeoe

import java.net.InetSocketAddress

import android.app.{Notification, Service}
import android.content.Intent
import android.os.IBinder
import com.taisukeoe.voip.{NetworkEvent, NetworkEventBroadcaster, VoIPSystem}

import scala.concurrent.Future
import scala.util._

/**
  * Created by taisukeoe on 17/04/15.
  */
object VoIPService {
  lazy val ACTION_INITIALIZE_TALK = "initialize_talk"
  lazy val ACTION_ACTIVATE_TALK = "activate_talk"
  lazy val ACTION_DEACTIVATE_TALK = "deactivate_talk"

  lazy val address = "address"
  lazy val port = "port"

  lazy val processId = 1234
}

class VoIPService extends Service {
  override def onBind(intent: Intent): IBinder = null

  private var voipSystem: Option[VoIPSystem] = None

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    intent.getAction match {
      case VoIPService.ACTION_INITIALIZE_TALK =>
        val listener = new NetworkEventBroadcaster(getApplicationContext)
        voipSystem = Option(VoIPSystem("voip", listener))
        import scala.concurrent.ExecutionContext.Implicits.global
        Future {
          val address = new InetSocketAddress(intent.getStringExtra(VoIPService.address), intent.getIntExtra(VoIPService.port, 80))
          voipSystem.foreach(_.connect(address))
        }.onComplete {
          case Success(_) => startForeground(VoIPService.processId, notification)
          case Failure(t) => listener.onEvent(NetworkEvent.ConnectionFailed)
        }

      case VoIPService.ACTION_ACTIVATE_TALK =>
        voipSystem.foreach(_.activateRecord())

      case VoIPService.ACTION_DEACTIVATE_TALK =>
        voipSystem.foreach(_.deactivateRecord())
    }
    Service.START_STICKY
  }

  private def notification: Notification = {
    new Notification.Builder(getApplicationContext).setContentTitle("VoIP Akka sample").setSmallIcon(android.R.drawable.ic_btn_speak_now).build()
  }

  override def onDestroy(): Unit = {
    voipSystem.foreach(_.release())
    voipSystem = None
    super.onDestroy()
  }
}
