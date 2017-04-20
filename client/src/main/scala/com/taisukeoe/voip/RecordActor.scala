package com.taisukeoe.voip

import akka.actor.{Actor, ActorRef, Stash}
import akka.util.ByteString
import android.media.{AudioFormat, AudioRecord}
import com.taisukeoe.config.AudioSource

import scala.concurrent.duration._

/**
  * Created by taisukeoe on 17/04/15.
  */
class RecordActor(nextActor: ActorRef) extends Actor with Stash {
  private val ringBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2
  private val recorder: AudioRecord = new AudioRecord(AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, ringBufferSize)
  private lazy val ringBuffer = new Array[Byte](ringBufferSize)
  private lazy val errorCountThreshold = 10
  private lazy val reinitializationDelay = 100 millis

  import context.dispatcher

  override def receive: Receive = receiveWithErrorAccumulating(0)

  val log = AndroidLogging(context.system.eventStream, "RecordActor")

  @scala.throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    context become receiveWithErrorAccumulating(0)
  }

  def receiveWithErrorAccumulating(errorCount: Int): Receive = {
    case Record =>
      if (recorder.getRecordingState == AudioRecord.RECORDSTATE_STOPPED)
        try {
          log.info("AudioRecord initialization")
          recorder.startRecording()
          self ! Record
        } catch {
          case ex: IllegalStateException =>
            if (errorCount > errorCountThreshold) {
              stash()
              throw AudioRecordInitializationException()
            } else {
              log.warning(s"AudioRecord initialization failed. Error count:$errorCount Reason:${ex.getMessage}")
              context become receiveWithErrorAccumulating(errorCount + 1)
              context.system.scheduler.scheduleOnce(reinitializationDelay, self, Record)
            }
        }
      else {
        val len = recorder.read(ringBuffer, 0, ringBufferSize)
        if (len >= 0) {
          nextActor ! ByteString(ringBuffer.take(len))
          self ! Record
        } else {
          stash()
          throw new AudioRecordException(len)
        }
      }
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit =
    if (recorder.getRecordingState != AudioRecord.RECORDSTATE_STOPPED) {
      recorder.stop()
      recorder.release()
    }
}

case object Record

case class AudioRecordInitializationException() extends Exception("AudioRecord cannot be initialized properly")

case class AudioRecordException(value: Int) extends Exception(s"AudioRecord read error. $value")