package xcom.niteshray.apps.collegeapp.model

data class Complaint(
    val id: String = "",
    val text: String = "",
    val userId: String = "",
    val originalUsername: String = "",
    val timestamp: Long = 0,
    val anonymous: Boolean = false,
    val isResolved: Boolean = false,
    val department : String ,
    val votesToReveal: List<String> = listOf()
){
    constructor() : this ("","","","",0,false,false,"", listOf())
}