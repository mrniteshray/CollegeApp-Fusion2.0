package xcom.niteshray.apps.collegeapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MidnightReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefs = context.getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean("enteredCampusToday", false)
        editor.putBoolean("exitedCampusToday", false)
        editor.putString("savedDate", getCurrentDate())
        editor.apply()
    }

    private fun getCurrentDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date())
    }
}
