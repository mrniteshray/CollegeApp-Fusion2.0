package xcom.niteshray.apps.collegeapp.model

data class Facility(
    var id : String,
    val name : String,
    val FacilityImgUrl: String,
    val Availability : Boolean
){
    constructor() : this("", "", "",false)
}
