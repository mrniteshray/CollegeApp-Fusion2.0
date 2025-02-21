package xcom.niteshray.apps.collegeapp.model

import android.os.Parcel
import android.os.Parcelable

data class Event(
    val id: String = "",
    val title: String = "",
    val clubId: String = "",
    val eventImg: String = "",
    val totalBudget: Int = 0,
    val sponsorships: List<Sponsor> = listOf(),
    val expenses: List<Expense> = listOf(),
    val roles: Roles = Roles(),
    val timestamp: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createTypedArrayList(Sponsor) ?: listOf(),
        parcel.createTypedArrayList(Expense) ?: listOf(),
        parcel.readParcelable(Roles::class.java.classLoader) ?: Roles(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(clubId)
        parcel.writeString(eventImg)
        parcel.writeInt(totalBudget)
        parcel.writeTypedList(sponsorships)
        parcel.writeTypedList(expenses)
        parcel.writeParcelable(roles, flags)
        parcel.writeString(timestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event = Event(parcel)
        override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
    }
}

data class Sponsor(
    val sponsor: String = "",
    val amount: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sponsor)
        parcel.writeInt(amount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Sponsor> {
        override fun createFromParcel(parcel: Parcel): Sponsor = Sponsor(parcel)
        override fun newArray(size: Int): Array<Sponsor?> = arrayOfNulls(size)
    }
}

data class Expense(
    val id: String = "",
    val item: String = "",
    val cost: Int = 0,
    val proofUrl: String = "",
    var approved: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte() // Read boolean
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(item)
        parcel.writeInt(cost)
        parcel.writeString(proofUrl)
        parcel.writeByte(if (approved) 1 else 0) // Write boolean
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense = Expense(parcel)
        override fun newArray(size: Int): Array<Expense?> = arrayOfNulls(size)
    }
}

data class Roles(
    val admin: List<ClubMembers> = listOf(),
    val members: List<ClubMembers> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(ClubMembers) ?: listOf(),
        parcel.createTypedArrayList(ClubMembers) ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(admin)
        parcel.writeTypedList(members)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Roles> {
        override fun createFromParcel(parcel: Parcel): Roles = Roles(parcel)
        override fun newArray(size: Int): Array<Roles?> = arrayOfNulls(size)
    }
}

data class ClubMembers(
    val id: String = "",
    val name: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ClubMembers> {
        override fun createFromParcel(parcel: Parcel): ClubMembers = ClubMembers(parcel)
        override fun newArray(size: Int): Array<ClubMembers?> = arrayOfNulls(size)
    }
}