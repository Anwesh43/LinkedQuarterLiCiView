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
val sizeFactor : Float = 3f
val qcdeg : Float = 60f
val DELAY : Long = 25

fun Int.getInverse() : Float = 1f / this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(0f, this - i * n.getInverse())) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.getInverse() + scaleFactor() * b.getInverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * scGap * dir

fun Canvas.drawSquareWithDiag(y : Float, size : Float, scale : Float, paint : Paint) {
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val lines : Int = 4
    val diags : Int = 2
    for (i in 0..1) {
        save()
        translate(0f, y * (1 - 2 * i))
        for (j in 0..lines) {
            val sc : Float = sc1.divideScale(j, lines)
            save()
            rotate(90f * j)
            drawLine(size, -size, size, -size + 2 * size * sc, paint)
            restore()
        }
        for (j in 0..diags) {
            val sc : Float = sc2.divideScale(j, diags)
            save()
            translate(0f, 0f)
            rotate(90f * j)
            drawLine(-size * sc, size * sc, size * sc, -size * sc, paint)
            restore()
        }
        restore()
    }
}

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
    drawSquareWithDiag(0.3f * h, size, scale, paint)
    rotate(90f * sc2)
    for (j in 0..(lines - 1)) {
        save()
        rotate(90f * j)
        val scj : Float = sc1.divideScale(j, lines)
        val scj1 : Float = scj.divideScale(0, 2)
        val scj2 : Float = scj.divideScale(1, 2)
        drawLine(size - lSize * scj1, 0f, size + lSize * scj1, 0f, paint)
        drawArc(RectF(-size, -size, size, size), startDeg, qcdeg * scj2, false, paint)
        restore()
    }
    restore()
}

class QuarterLiCiView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)
    var onCompleteListener : OnAnimationCompleteListener? = null

    fun addOnCompleteListener(onComplete: (Int) -> Unit, onReset: (Int) -> Unit) {
        onCompleteListener = OnAnimationCompleteListener(onComplete, onReset)
    }

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, lines * 2, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(DELAY)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class QLCNode(var i : Int, val state : State = State()) {

        private var next : QLCNode? = null
        private var prev : QLCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (this.i < nodes - 1) {
                next = QLCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawQLCNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : QLCNode {
            var curr : QLCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class QuarterLiCi(var i : Int) {
        private var dir : Int = 1

        private var curr : QLCNode = QLCNode(0)

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : QuarterLiCiView) {

        private val animator : Animator = Animator(view)
        private val qlc : QuarterLiCi = QuarterLiCi(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            qlc.draw(canvas, paint)
            animator.animate {
                qlc.update {i, scl ->
                    animator.stop()
                    when (scl) {
                        1f -> view.onCompleteListener?.onComplete?.invoke(i)
                        0f -> view.onCompleteListener?.onReset?.invoke(i)
                    }
                }
            }
        }

        fun handleTap() {
            qlc.startUpdating {
                animator.start()
            }
        }
    }

    data class OnAnimationCompleteListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)

    companion object {
        fun create(activity: Activity) : QuarterLiCiView {
            val view : QuarterLiCiView = QuarterLiCiView(activity)
            activity.setContentView(view)
            return view
        }
    }
}