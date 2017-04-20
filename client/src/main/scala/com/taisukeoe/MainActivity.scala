package com.taisukeoe

import android.app.Activity
import android.content.pm.PackageManager
import android.content.{BroadcastReceiver, Intent}
import android.os.{Build, Bundle}
import android.widget.CompoundButton
import com.taisukeoe.TypedResource._
import com.taisukeoe.voip.{NetworkEvent, NetworkEventBroadcastReceiver, NetworkEventListener}

/**
  * Created by taisukeoe on 17/04/15.
  */
class MainActivity extends Activity {

  private var receiver: Option[BroadcastReceiver] = None
  private lazy val recordRequestCode = 1234

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    //Android 6.0 or above requires explicit permission for audio recording. Otherwise AudioRecord initialization always fails.
    if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
      requestPermissions(Array(android.Manifest.permission.RECORD_AUDIO), recordRequestCode)
    else
      initialize()
  }


  override def onRequestPermissionsResult(requestCode: Int, permissions: Array[String], grantResults: Array[Int]): Unit = {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if ((permissions zip grantResults).contains((android.Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED)))
      initialize()
    else
      Option(MainActivity.this.findView(TR.status)).foreach(_.setText(s"[ERROR]RECORD_AUDIO permission is not granted."))
  }

  private def initialize(): Unit = {
    import Implicits._

    this.findView(TR.connect).onClick { _ =>
      startService(startIntent)
    }

    this.findView(TR.ptt).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener {
      override def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean): Unit =
        if (isChecked)
          startService(intent(VoIPService.ACTION_ACTIVATE_TALK))
        else
          startService(intent(VoIPService.ACTION_DEACTIVATE_TALK))
    })

    val r = new NetworkEventBroadcastReceiver(getApplicationContext, new NetworkEventListener {
      override def onEvent(networkEvent: NetworkEvent): Unit = networkEvent match {
        case NetworkEvent.Connected(address) =>
          Option(MainActivity.this.findView(TR.status)).foreach(_.setText(s"Connected:${address}"))
        case NetworkEvent.Disconnected(address) =>
          Option(MainActivity.this.findView(TR.status)).foreach(_.setText(s"Disconnected:${address}"))
        case NetworkEvent.ConnectionFailed =>
          Option(MainActivity.this.findView(TR.status)).foreach(_.setText(s"Connection failed!"))
        case NetworkEvent.Error(address) =>
      }
    })
    registerReceiver(r, NetworkEventBroadcastReceiver.filter)
    receiver = Option(r)
  }

  override def onDestroy(): Unit = {
    stopService(stopIntent)
    receiver.foreach(unregisterReceiver)
    receiver = None
    super.onDestroy()
  }

  def intent(action: String): Intent = {
    val i = new Intent(getApplicationContext, classOf[VoIPService])
    i.setAction(action)
    i
  }

  def startIntent: Intent = {
    val i = intent(VoIPService.ACTION_INITIALIZE_TALK)
    i.putExtra(VoIPService.address, this.findView(TR.address).getText.toString)
    i.putExtra(VoIPService.port, this.findView(TR.port).getText.toString.toInt)
    i
  }

  def stopIntent: Intent = new Intent(getApplicationContext, classOf[VoIPService])
}
