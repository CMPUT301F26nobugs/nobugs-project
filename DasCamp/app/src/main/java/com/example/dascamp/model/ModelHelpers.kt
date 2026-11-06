package com.example.dascamp.model

fun formatPrice(price: Double): String =
    if (price % 1.0 == 0.0) price.toInt().toString() else "%.2f".format(price)

fun invalidDate(value: String): Boolean {
    if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)) return true
    val parts = value.split("-")
    val month = parts[1].toInt()
    val day = parts[2].toInt()
    return month !in 1..12 || day !in 1..31
}