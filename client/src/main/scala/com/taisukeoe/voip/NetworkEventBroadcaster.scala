package com.taisukeoe.voip

import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}

/**
  * Created by taisukeoe on 17/04/19.
  */
class NetworkEventBroadcaster(androidContext: Context) extends NetworkEventListener {
  def onEvent(networkEvent: NetworkEvent): Unit = androidContext.sendBroadcast(intent(networkEvent))

  private def intent(event: NetworkEvent) = {
    val i = new Intent()
    i.setAction(NetworkEvent.ACTION)
    i.putExtra(NetworkEvent.eventKey, event)
    i
  }
}

object NetworkEventBroadcastReceiver {
  lazy val filter = new IntentFilter(NetworkEvent.ACTION)
}

class NetworkEventBroadcastReceiver(androidContext: Context, listener: NetworkEventListener) extends BroadcastReceiver {
  override def onReceive(context: Context, intent: Intent): Unit = intent.getAction match {
    case NetworkEvent.ACTION =>
      listener.onEvent(intent.getSerializableExtra(NetworkEvent.eventKey).asInstanceOf[NetworkEvent])
  }
}
