package com.example.dascamp.model

import com.google.firebase.firestore.Exclude

/**
 * The roles a user can hold. One user can have several at once
 */
enum class Role { ENTRANT, OWNER, ADMIN}

/**
 * The entrant account shown on the dashboard and edited on the profile screen
 * An admin may also act as an entrant with one profile
 */
data class EntrantProfile(
    val id: String = "",              // Firebase uid
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUri: String? = null,
    val notificationsOn: Boolean = true
)

/**
 * The owner account shown on the dashboard and edited on the profile screen
 * An admin may also act as an owner with one profile
 */
data class OwnerProfile(
    val id: String = "",              // same uid as their EntrantProfile, if they have one
    val organizationName: String = "",
    val contactName: String = "",
    val email: String = "",
    val phone: String = "",
    val notificationsOn: Boolean = true
)

/**
 * The admin account shown on the dashboard and edited on the profile screen
 * An admin may also act as an owner and entrant with one profile
 */
data class AdminProfile(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val notificationsOn: Boolean = true
)