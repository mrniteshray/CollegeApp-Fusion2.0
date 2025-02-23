package xcom.niteshray.apps.collegeapp.model

import android.os.Parcel
import android.os.Parcelable

data class User(
    val name: String,
    val email: String,
    val profilePic: String,
    val authId: String,
    val role: String,
    val branch: String,
    val upperAuthority: String?,
    val parentemail: String?
) : Parcelable {
    constructor() : this("", "", "", "", "", "", null, null)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(), // Read nullable String
        parcel.readString()  // Read nullable String
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(profilePic)
        parcel.writeString(authId)
        parcel.writeString(role)
        parcel.writeString(branch)
        parcel.writeString(upperAuthority) // Write nullable String
        parcel.writeString(parentemail)  // Write nullable String
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}