package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kizitonwose.calendar.view.CalendarView
import java.time.DayOfWeek
import java.time.YearMonth

import com.kizitonwose.calendar.core.CalendarDay
import android.graphics.Color
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate

class AgendaFragment : Fragment() {

    class DayViewContainer(view: View) : ViewContainer(view) {

        val txtDia = view.findViewById<TextView>(R.id.txtDia)

        val indicadorTreino =
            view.findViewById<View>(R.id.indicadorTreino)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_agenda,
            container,
            false
        )

        val txtMesAno = view.findViewById<TextView>(
            R.id.txtMesAno
        )

        val diasTreino = setOf(
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(3),
            LocalDate.now().minusDays(5),
            LocalDate.now().minusDays(7)
        )

        // AQUI
        val calendarView =
            view.findViewById<CalendarView>(R.id.calendarView)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)

        val meses = arrayOf(
            "Janeiro",
            "Fevereiro",
            "Março",
            "Abril",
            "Maio",
            "Junho",
            "Julho",
            "Agosto",
            "Setembro",
            "Outubro",
            "Novembro",
            "Dezembro"
        )

        txtMesAno.text =
            "${meses[currentMonth.monthValue - 1]} ${currentMonth.year}"

        calendarView.setup(
            startMonth,
            endMonth,
            DayOfWeek.MONDAY
        )

        calendarView.scrollToMonth(currentMonth)

        // E AQUI
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {

                override fun create(view: View): DayViewContainer {
                    return DayViewContainer(view)
                }

            override fun bind(
                container: DayViewContainer,
                day: CalendarDay
            ) {

                if (day.position == DayPosition.MonthDate) {

                    container.txtDia.visibility = View.VISIBLE

                    container.txtDia.text =
                        day.date.dayOfMonth.toString()

                    container.txtDia.background = null

                    container.txtDia.setTextColor(Color.WHITE)

                    container.indicadorTreino.visibility = View.GONE

                    if (day.date == LocalDate.now()) {

                        container.txtDia.setBackgroundResource(
                            R.drawable.bg_dia_hoje
                        )

                        container.txtDia.setTextColor(Color.BLACK)
                    }

                    if (day.date in diasTreino) {

                        container.indicadorTreino.visibility =
                            View.VISIBLE
                    }

                } else {

                    container.txtDia.visibility = View.INVISIBLE
                    container.indicadorTreino.visibility = View.GONE
                }
            }
            }

        return view
    }

}

