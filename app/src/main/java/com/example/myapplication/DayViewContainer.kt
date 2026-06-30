package com.example.myapplication

import android.view.View
import android.widget.TextView
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View) : ViewContainer(view) {

    val txtDia = view.findViewById<TextView>(R.id.txtDia)

    val indicadorTreino = view.findViewById<View>(
        R.id.indicadorTreino
    )
}