package com.taisukeoe.voip

import akka.actor.Actor
import android.media.{AudioFormat, AudioManager, AudioTrack}

/**
  * Created by taisukeoe on 17/04/15.
  */
class PlayActor() extends Actor {
  private val trackBufSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4

  private val track: AudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, trackBufSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE)

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    track.play()
  }

  override def receive: Receive = {
    case AudioPacketMessage(content) =>
      track.write(content.toArray[Byte], 0, content.length)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    track.stop()
    track.flush()
    track.release()
  }
}