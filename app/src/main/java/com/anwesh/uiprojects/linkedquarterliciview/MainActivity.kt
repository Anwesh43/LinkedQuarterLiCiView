package com.anwesh.uiprojects.linkedquarterliciview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.quarterliciview.QuarterLiCiView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QuarterLiCiView.create(this)
    }
}
