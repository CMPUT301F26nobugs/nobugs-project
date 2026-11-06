package com.example.dascamp.model

import com.google.firebase.firestore.Exclude

/**
 * The roles a user can hold. One user can have several at once
 */
enum class Role { ENTRANT, OWNER, ADMIN}

/**
 * PROFILE
 * What every user had, whatever their role
 */
interface Profile {
    val id: String
    val firstName: String
    val lastName: String
    val email: String
    val phoneNumber: String
    val notificationsOn: Boolean
    //val profileImageUri: String? *TODO: Implement image uploads

    // The name to show on screen
    fun displayName(): String = "$firstName $lastName".trim()
}

/**
 * ENTRANT
 * The entrant account shown on the dashboard and edited on the profile screen
 * An admin may also act as an entrant with one profile
 */
data class EntrantProfile(
    override val id: String = "", // Firebase uid
    override val firstName: String = "",
    override val lastName: String = "",
    override val email: String = "",
    override val phoneNumber: String = "",
    // override val profileImageUri: String? = null, *TODO: Implement image upload
    override val notificationsOn: Boolean = true
) : Profile

/**
 * PROFILE
 * The owner account shown on the dashboard and edited on the profile screen
 * An admin may also act as an owner with one profile
 */
data class OwnerProfile(
    override val id: String = "", // same uid as their EntrantProfile, if they have one
    override val firstName: String = "",
    override val lastName: String = "",
    override val email: String = "",
    override val phoneNumber: String = "",
    override val notificationsOn: Boolean = true,
    // Add profile image when photo upload is implemented
    val organizationName: String = ""
) : Profile

/**
 * The admin account shown on the dashboard and edited on the profile screen
 * An admin may also act as an owner and entrant with one profile
 */
data class AdminProfile(
    override val id: String = "",
    override val firstName: String = "",
    override val lastName: String = "",
    override val email: String = "",
    override val phoneNumber: String = "",
    override val notificationsOn: Boolean = true,
    // Add profile image when photo upload is implemented
    val actsAsOwner: Boolean = false,
    val actsAsEntrant: Boolean = false
) : Profile