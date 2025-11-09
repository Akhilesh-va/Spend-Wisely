package com.example.mindfullexpenses.core.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

object CurrencyFormatter {
    private val locale = Locale("en", "IN")

    fun formatMinor(amountMinor: Long, currencyCode: String = "INR"): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = java.util.Currency.getInstance(currencyCode)
        return format.format(amountMinor / 100.0)
    }

    fun formatMinorCompact(amountMinor: Long, currencyCode: String = "INR"): String {
        val abs = amountMinor.absoluteValue
        val rupees = abs / 100.0
        val compact = when {
            rupees >= 1_00_000 -> String.format(locale, "%.1fL", rupees / 1_00_000)
            rupees >= 1_000 -> String.format(locale, "%.1fk", rupees / 1_000)
            else -> String.format(locale, "%.0f", rupees)
        }
        val symbol = java.util.Currency.getInstance(currencyCode).symbol
        val prefix = if (amountMinor < 0) "-" else ""
        return "$prefix$symbol$compact"
    }
}


