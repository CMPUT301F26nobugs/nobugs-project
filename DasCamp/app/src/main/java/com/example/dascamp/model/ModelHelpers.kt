package com.example.dascamp.model

/**
 * Formats a price for display, dropping the decimals when the value is a whole number.
 *
 * Examples: `25.0` becomes `"25"`, and `25.5` becomes `"25.50"`.
 */
fun formatPrice(price: Double): String =
    if (price % 1.0 == 0.0) price.toInt().toString() else "%.2f".format(price)

/**
 * Checks whether a date string is invalid.
 *
 * A valid value must match the `yyyy-MM-dd` format, with a month from 1 to 12 and
 * a day from 1 to 31. This is a basic check only: it does not verify that the day
 * exists in the given month, so values like `2026-02-30` are treated as valid.
 */
fun invalidDate(value: String): Boolean {
    if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)) return true
    val parts = value.split("-")
    val month = parts[1].toInt()
    val day = parts[2].toInt()
    return month !in 1..12 || day !in 1..31
}