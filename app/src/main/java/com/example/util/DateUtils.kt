package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.FRANCE)
    private val displayFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
    private val monthDisplayFormat = SimpleDateFormat("MMMM yyyy", Locale.FRANCE)

    fun getTodayIso(): String {
        return isoFormat.format(Date())
    }

    fun getCurrentMonthIso(): String {
        return monthFormat.format(Date())
    }

    fun formatFriendlyDate(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            val todayCal = Calendar.getInstance()
            val targetCal = Calendar.getInstance().apply { time = date }

            val diffDays = (todayCal.get(Calendar.YEAR) - targetCal.get(Calendar.YEAR)) * 365 +
                    (todayCal.get(Calendar.DAY_OF_YEAR) - targetCal.get(Calendar.DAY_OF_YEAR))

            val shortDate = SimpleDateFormat("d MMM", Locale.FRANCE).format(date)
            when (diffDays) {
                0 -> "Aujourd'hui ($shortDate)"
                1 -> "Hier ($shortDate)"
                -1 -> "Demain ($shortDate)"
                else -> {
                    val dayName = SimpleDateFormat("EEE d MMM", Locale.FRANCE).format(date)
                        .replaceFirstChar { it.uppercase() }
                    dayName
                }
            }
        } catch (e: Exception) {
            isoDate
        }
    }

    fun shiftDays(isoDate: String, offsetDays: Int): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_MONTH, offsetDays)
            }
            isoFormat.format(cal.time)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun shiftMonth(yearMonthIso: String, offsetMonths: Int): String {
        return try {
            val date = monthFormat.parse(yearMonthIso) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, offsetMonths)
            }
            monthFormat.format(cal.time)
        } catch (e: Exception) {
            yearMonthIso
        }
    }

    fun formatMonthTitle(yearMonthIso: String): String {
        return try {
            val date = monthFormat.parse(yearMonthIso) ?: Date()
            monthDisplayFormat.format(date).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            yearMonthIso
        }
    }

    fun getMonthDateRange(yearMonthIso: String): Pair<String, String> {
        val cal = Calendar.getInstance().apply {
            time = (try { monthFormat.parse(yearMonthIso) } catch (e: Exception) { null }) ?: Date()
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val start = isoFormat.format(cal.time)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, maxDay)
        val end = isoFormat.format(cal.time)
        return Pair(start, end)
    }

    fun getAllDaysInMonth(yearMonthIso: String): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance().apply {
            time = (try { monthFormat.parse(yearMonthIso) } catch (e: Exception) { null }) ?: Date()
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDay) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            list.add(isoFormat.format(cal.time))
        }
        return list
    }

    fun getWeekStartIso(dateIso: String): String {
        return try {
            val date = isoFormat.parse(dateIso) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
            }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val daysToMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            cal.add(Calendar.DAY_OF_MONTH, -daysToMonday)
            isoFormat.format(cal.time)
        } catch (e: Exception) {
            dateIso
        }
    }

    fun getCurrentWeekStartIso(): String {
        return getWeekStartIso(getTodayIso())
    }

    fun getDaysInWeek(weekMondayIso: String): List<String> {
        val list = mutableListOf<String>()
        for (i in 0..6) {
            list.add(shiftDays(weekMondayIso, i))
        }
        return list
    }

    fun shiftWeek(weekMondayIso: String, offsetWeeks: Int): String {
        return shiftDays(weekMondayIso, offsetWeeks * 7)
    }

    fun formatWeekTitle(weekMondayIso: String): String {
        return try {
            val mondayDate = isoFormat.parse(weekMondayIso) ?: return weekMondayIso
            val sundayIso = shiftDays(weekMondayIso, 6)
            val sundayDate = isoFormat.parse(sundayIso) ?: return weekMondayIso

            val calMon = Calendar.getInstance().apply { time = mondayDate }
            val calSun = Calendar.getInstance().apply { time = sundayDate }

            val dayMon = calMon.get(Calendar.DAY_OF_MONTH)
            val daySun = calSun.get(Calendar.DAY_OF_MONTH)
            val monthMon = SimpleDateFormat("MMM", Locale.FRANCE).format(mondayDate).replace(".", "")
            val monthSun = SimpleDateFormat("MMM", Locale.FRANCE).format(sundayDate).replace(".", "")
            val yearMon = calMon.get(Calendar.YEAR)
            val yearSun = calSun.get(Calendar.YEAR)

            if (calMon.get(Calendar.MONTH) == calSun.get(Calendar.MONTH) && yearMon == yearSun) {
                "Semaine du $dayMon au $daySun $monthMon $yearSun"
            } else if (yearMon == yearSun) {
                "Semaine du $dayMon $monthMon au $daySun $monthSun $yearSun"
            } else {
                "Semaine du $dayMon $monthMon $yearMon au $daySun $monthSun $yearSun"
            }
        } catch (e: Exception) {
            weekMondayIso
        }
    }

    fun getDayOfWeekShort(dateIso: String): String {
        return try {
            val date = isoFormat.parse(dateIso) ?: return ""
            val day = SimpleDateFormat("EEE", Locale.FRANCE).format(date)
            day.replace(".", "").replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            ""
        }
    }

    fun isCurrentWeek(weekMondayIso: String): Boolean {
        return getCurrentWeekStartIso() == weekMondayIso
    }
}
