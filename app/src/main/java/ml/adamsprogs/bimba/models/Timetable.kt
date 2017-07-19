package ml.adamsprogs.bimba.models

import android.content.Context
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File

class Timetable(var context: Context) {
    var db: SQLiteDatabase? = null

    fun refresh() {
        readDbFile()
    }

    private fun readDbFile() {
        try {
            db = SQLiteDatabase.openDatabase(File(context.filesDir, "new_timetable.db").path,
                    null, SQLiteDatabase.OPEN_READONLY)
        } catch(e: SQLiteCantOpenDatabaseException) {
            Log.e("Timetable", "Cannot open db")
            db = null
        }
    }

    fun getStops(): ArrayList<StopSuggestion>? {
        if (db == null)
            return null
        val cursor = db!!.rawQuery("select name ||char(10)|| headsigns as suggestion, id from stops" +
                " join nodes on(stops.symbol = nodes.symbol) order by name, id;", null)
        val stops = ArrayList<StopSuggestion>()
        while (cursor.moveToNext())
            stops.add(StopSuggestion(cursor.getString(0), cursor.getString(1)))
        cursor.close()
        return stops
    }

    fun getStopName(stopId: String): String? {
        if (db == null)
            return null
        val cursor = db!!.rawQuery("select name from nodes join stops on(stops.symbol = nodes.symbol) where id = ?;",
                listOf(stopId).toTypedArray())
        val name: String
        cursor.moveToNext()
            name = cursor.getString(0)
        cursor.close()
        return name
    }

    fun getStopDepartures(stopId: String): HashMap<String, ArrayList<Departure>>? {
        if (db == null)
            return null
        val cursor = db!!.rawQuery("select lines.number, mode, substr('0'||hour, -2) || ':' || " +
            "substr('0'||minute, -2) as time, lowFloor, modification, headsign from departures join "+
            "timetables on(timetable_id = timetables.id) join lines on(line_id = lines.id) where "+
            "stop_id = ? order by mode, time;", listOf(stopId).toTypedArray())
        val departures = HashMap<String, ArrayList<Departure>>()
        departures.put("workdays", ArrayList())
        departures.put("saturdays", ArrayList())
        departures.put("sundays", ArrayList())
        while (cursor.moveToNext()) {
            departures[cursor.getString(1)]?.add(Departure(cursor.getString(0),
                    cursor.getString(1), cursor.getString(2), cursor.getInt(3) == 1,
                    cursor.getString(4), cursor.getString(5)))
        }
        cursor.close()
        return departures
    }

    init {
        readDbFile()
    }

}