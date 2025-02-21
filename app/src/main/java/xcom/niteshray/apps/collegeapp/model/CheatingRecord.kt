package xcom.niteshray.apps.collegeapp.model

data class CheatingRecord(
    val id: String = "",
    val studentImg: String = "",
    val studentid : String,
    val studentName: String = "",
    val reason: String = "",
    val proofUrl: String = "",
    val reportedBy: String = "",
    val reportedById: String = "",
    val timestamp: String = "",
    val appealStatus: String = "Pending",
    val appealMessage: String? = null,
    val appealedBy: String? = null
){
    constructor() : this("", "", "", "", "", "", "", "", "", "", null, null)
}
