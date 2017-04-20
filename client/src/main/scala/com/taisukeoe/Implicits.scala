package com.taisukeoe

import android.view.{MotionEvent, View}
import android.view.View.OnTouchListener

/**
  * Created by taisukeoe on 17/04/15.
  */
object Implicits {
  implicit class view2OnTouchFunction(undelying: View) {
    def onTouch(f: (View, MotionEvent) => Boolean) = undelying.setOnTouchListener(new OnTouchListener {
      override def onTouch(v: View, event: MotionEvent): Boolean = f(v, event)
    })
  }
  implicit class view2OnClickFunction(undelying: View) {
    def onClick(f: View => Unit) = undelying.setOnClickListener(new View.OnClickListener(){
      override def onClick(v: View): Unit = f(v)
    })
  }
}
