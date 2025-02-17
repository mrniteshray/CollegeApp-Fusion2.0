package xcom.niteshray.apps.collegeapp.model

data class User(
    val name: String,
    val email: String,
    val profilePic: String,
    val authId: String,
    val role: String,
    val branch: String,
    val upperAuthority: String?,
    val parentemail: String?
) {
    constructor() : this("", "", "", "", "", "", null, null)
}