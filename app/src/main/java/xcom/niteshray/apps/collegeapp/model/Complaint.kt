package xcom.niteshray.apps.collegeapp.model

data class Complaint(
    val id: String = "",
    val text: String = "",
    val userId: String = "",
    val originalUserId: String = "",
    val timestamp: Long = 0,
    val anonymous: Boolean = false,
    val status: String = "Pending",
    val votesToReveal: List<String> = listOf()
)