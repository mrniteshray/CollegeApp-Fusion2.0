package xcom.niteshray.apps.collegeapp.model

data class Candidate(
    val candidateId: String = "",
    val name: String = "",
    val profilePic: String = "",
    val bio: String = "",
    val votedUsers: List<String> = emptyList()
)

{
    constructor() : this ("", "", "", "", emptyList())
}
