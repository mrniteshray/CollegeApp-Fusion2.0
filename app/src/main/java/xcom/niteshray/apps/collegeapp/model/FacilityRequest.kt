package xcom.niteshray.apps.collegeapp.model

data class FacilityRequest(
    val requestId: String = "", // Unique ID
    val facilityId: String = "",
    val facilityName: String = "",
    val facilityImage: String = "",
    val bookedBy: String = "",
    val bookedById: String = "",
    val bookingDate: String = "",
    val status: String = "pending", // pending, approved, rejected
    val approvedBy: String? = null, // Who last approved it
    val upperAuthority: String? = null // Next person who needs to approve
)

