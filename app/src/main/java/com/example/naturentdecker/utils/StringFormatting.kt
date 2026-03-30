package com.example.naturentdecker.utils

// Can be handled with localization
fun String.formatAsPrice(): String =
    toDoubleOrNull()?.let { "€%.2f".format(it) } ?: "€$this"