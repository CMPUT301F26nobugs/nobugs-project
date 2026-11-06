package com.example.dascamp.model

/**
 * One campsite listing owned by one organization
 * Entrant search reads this same type. Admin removal deletes one of these rows
 */
data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val maxOccupancy: Int = 0,
    val pricePerNight: Double = 0.0,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val imageUri: String? = null,
    val rules: String = "",
    val cancellationPolicy: String = "",
    val tags: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val availability: List<DateRange> = emptyList()
)

/**
 * Dates are stored as yyyy-MM-dd
 * defaults to an empty string as firestore builds on an empty constructor
 */
data class DateRange(
    val start: String = "",
    val end: String = ""
)

data class CampsiteBuild(val campsite: Campsite?, val error: String?)

/**
 * The add and edit screens share this draft so both forms ask for the same fields
 */
data class CampsiteFormState(
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val maxOccupancy: String = "",
    val pricePerNight: String = "",
    val rules: String = "",
    val cancellationPolicy: String = "",
    val tags: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val imageUri: String? = null,
    val availability: List<DateRange> = listOf(DateRange("", ""))
) {
    companion object {
        fun from(campsite: Campsite) = CampsiteFormState(
            name = campsite.name,
            location = campsite.location,
            description = campsite.description,
            maxOccupancy = campsite.maxOccupancy.toString(),
            pricePerNight = formatPrice(campsite.pricePerNight),
            rules = campsite.rules,
            cancellationPolicy = campsite.cancellationPolicy,
            tags = campsite.tags.joinToString(", "),
            latitude = campsite.latitude.toString(),
            longitude = campsite.longitude.toString(),
            imageUri = campsite.imageUri,
            availability = campsite.availability.ifEmpty { listOf(DateRange("", "")) }
        )
    }

    /**
     * Turns the form into a campsite
     * Rating is passed in so an edit cannot wipe reviews entrants already gave
     * Rules are required because the entrant has to accept them before reserving
     */
    fun toCampsite(
        id: String,
        ownerId: String,
        rating: Double,
        ratingCount: Int
    ): CampsiteBuild {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return CampsiteBuild(null, "Enter a campsite name")
        val trimmedLocation = location.trim()
        if (trimmedLocation.isEmpty()) {
            return CampsiteBuild(null, "Enter the location")
        }
        val trimmedDescription = description.trim()
        if (trimmedDescription.isEmpty()) {
            return CampsiteBuild(null, "Describe the amenities")
        }
        val occupancy = maxOccupancy.trim().toIntOrNull()
        if (occupancy == null || occupancy < 1) {
            return CampsiteBuild(null, "Enter a maximum occupancy of at least 1.")
        }
        val price = pricePerNight.trim().toDoubleOrNull()
        if (price == null || price < 0) {
            return CampsiteBuild(null, "Enter a price per night")
        }
        val trimmedRules = rules.trim()
        if (trimmedRules.isEmpty()) {
            return CampsiteBuild(null, "Enter the rules entrants must accept before they reserve")
        }
        val trimmedPolicy = cancellationPolicy.trim()
        if (trimmedPolicy.isEmpty()) {
            return CampsiteBuild(null, "Enter a cancellation policy")
        }
        val lat = latitude.trim().toDoubleOrNull()
        if (lat == null || lat !in -90.0..90.0) {
            return CampsiteBuild(null, "Enter a latitude between -90 and 90")
        }
        val lng = longitude.trim().toDoubleOrNull()
        if (lng == null || lng !in -180.0..180.0) {
            return CampsiteBuild(null, "Enter a longitude between -180 and 180")
        }
        val ranges = availability.filter { it.start.isNotBlank() || it.end.isNotBlank() }
        if (ranges.isEmpty()) {
            return CampsiteBuild(null, "Add availability range, such as 2027-05-01 to 2027-05-02.")
        }
        ranges.forEach { range ->
            if (invalidDate(range.start) || invalidDate(range.end)) {
                return CampsiteBuild(
                    null,
                    "Use this format 2027-05-01 for every availability range"
                )
            }
            if (range.end < range.start) {
                return CampsiteBuild(
                    null,
                    "Each availability end date has to be on or after its start date"
                )
            }
        }
        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return CampsiteBuild(
            Campsite(
                id = id,
                ownerId = ownerId,
                name = trimmedName,
                location = trimmedLocation,
                description = trimmedDescription,
                maxOccupancy = occupancy,
                pricePerNight = price,
                rating = rating,
                ratingCount = ratingCount,
                imageUri = imageUri,
                rules = trimmedRules,
                cancellationPolicy = trimmedPolicy,
                tags = tagList,
                latitude = lat,
                longitude = lng,
                availability = ranges
            ),
            null
        )
    }
}

