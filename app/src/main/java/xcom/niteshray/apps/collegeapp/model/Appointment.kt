package xcom.niteshray.apps.collegeapp.model

data class Appointment(
    val bookingId: String = "",
    val studentAuthId: String = "",
    val studentEmail : String = "",
    val studentParentemail : String = "",
    val reason: String = "",
    val date: String = "",
    val time: String = "",
    val isAttended : Boolean = false
)
