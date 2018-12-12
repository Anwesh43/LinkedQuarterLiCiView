package com.anwesh.uiprojects.quarterliciview

/**
 * Created by anweshmishra on 12/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val color : Int = Color.parseColor("#0D47A1")
val strokeFactor : Int = 90
val sizeFactor : Float = 2.6f
val qcdeg : Float = 75f

fun Int.getInverse() : Float = 1f / this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(0f, this - i * n.getInverse())) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.getInverse() + scaleFactor() * b.getInverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * scGap * dir

fun Canvas.drawQLCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = color
    paint.style = Paint.Style.STROKE
    val size : Float = gap / sizeFactor
    val lSize : Float = size / 4
    val startDeg = (90f - qcdeg) / 2
    save()
    translate(gap * (i + 1), h/2)
    rotate(90f * sc2)
    for (j in 0..(lines - 1)) {
        val scj : Float = sc1.divideScale(j, lines)
        val scj1 : Float = scj.divideScale(0, 2)
        val scj2 : Float = scj.divideScale(1, 2)
        drawLine(size - lSize * scj1, 0f, size + lSize * scj2, 0f, paint)
        drawArc(RectF(-size, -size, size, size), startDeg, qcdeg * scj2, false, paint)
    }
    restore()
}