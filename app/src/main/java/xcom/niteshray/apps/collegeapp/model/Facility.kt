package xcom.niteshray.apps.collegeapp.model

import com.google.firebase.Timestamp
import android.os.Parcel
import android.os.Parcelable

data class Facility(
    val facilityId: String = "",
    val name: String = "",
    val bannerImage: String = "",
    val openTime: String = "",
    val closeTime: String = "",
    val guardId: String = "",
    val slots: List<Slot> = emptyList()
) : Parcelable {
    constructor() : this("", "", "", "", "", "", emptyList())
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(Slot) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(facilityId)
        parcel.writeString(name)
        parcel.writeString(bannerImage)
        parcel.writeString(openTime)
        parcel.writeString(closeTime)
        parcel.writeString(guardId)
        parcel.writeTypedList(slots)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Facility> {
        override fun createFromParcel(parcel: Parcel): Facility = Facility(parcel)
        override fun newArray(size: Int): Array<Facility?> = arrayOfNulls(size)
    }
}

data class Slot(
    val startTime: Timestamp,
    val endtime: Timestamp,
    val bookedById: String? = null,
    val bookedByName: String,
    val bookingid: String
) : Parcelable {
    constructor() : this(Timestamp.now(), Timestamp.now(), null, "", "")
    constructor(parcel: Parcel) : this(
        Timestamp(parcel.readLong(), parcel.readInt()), // Read Timestamp
        Timestamp(parcel.readLong(), parcel.readInt()), // Read Timestamp
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(startTime.seconds)      // Write Timestamp seconds
        parcel.writeInt(startTime.nanoseconds)   // Write Timestamp nanoseconds
        parcel.writeLong(endtime.seconds)        // Write Timestamp seconds
        parcel.writeInt(endtime.nanoseconds)     // Write Timestamp nanoseconds
        parcel.writeString(bookedById)
        parcel.writeString(bookedByName)
        parcel.writeString(bookingid)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Slot> {
        override fun createFromParcel(parcel: Parcel): Slot = Slot(parcel)
        override fun newArray(size: Int): Array<Slot?> = arrayOfNulls(size)
    }
}