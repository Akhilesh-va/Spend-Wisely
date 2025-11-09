package com.example.mindfullexpenses.core.autotrack.parser

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.toBigDecimalOrNull

@Singleton
class SmsParser @Inject constructor() {

    fun parse(address: String?, body: String?, timestampMillis: Long): ParsedExpense? {
        if (body.isNullOrBlank()) return null
        val patterns = bankPatterns
        for ((regex, metadata) in patterns) {
            val match = regex.find(body) ?: continue
            val amountMinor = match.groupValues.getOrNull(1)?.toAmountMinor() ?: continue
            val merchant = match.groupValues.getOrNull(2)?.ifBlank { address.orEmpty() } ?: address.orEmpty()
            val cleanMerchant = MerchantCleaner.clean(merchant)
            val isCredit = metadata.creditKeywords?.any { body.contains(it, ignoreCase = true) } == true
            return ParsedExpense(
                amountMinor = if (isCredit) -amountMinor else amountMinor,
                rawMerchant = merchant,
                cleanMerchant = cleanMerchant,
                paymentMethod = metadata.paymentMethod,
                timestamp = Instant.ofEpochMilli(timestampMillis),
                isCredit = isCredit
            )
        }

        return null
    }

    private fun String.toAmountMinor(): Long? {
        val sanitized = replace(",", "")
            .replace("INR", "", ignoreCase = true)
            .replace("Rs.", "", ignoreCase = true)
            .replace("Rs", "", ignoreCase = true)
            .replace("₹", "")
            .trim()
        return sanitized.toBigDecimalOrNull()?.multiply(HUNDRED)?.toLong()
    }

    private data class PatternMetadata(
        val paymentMethod: String,
        val creditKeywords: List<String>? = null
    )

    companion object {
        private val HUNDRED = java.math.BigDecimal(100)

        private val bankPatterns: Map<Regex, PatternMetadata> = mapOf(
            Regex(
                """debited\s+for\s*(?:rs\.?|inr)?[:\s]*([\d,]+(?:\.\d{1,2})?)(?:.*? by ([^0-9]+?)(?:\s?ref\b|$))?""",
                RegexOption.IGNORE_CASE
            ) to PatternMetadata(
                paymentMethod = "Bank SMS"
            ),
            Regex("debited (?:by|for) (?:rs\\.?|inr)?\\s*([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:from|at)\\s*(.+)", RegexOption.IGNORE_CASE) to PatternMetadata(
                paymentMethod = "Bank SMS"
            ),
            Regex("account .* debited for INR ([\\d,]+(?:\\.\\d{1,2})?).* to (.+)", RegexOption.IGNORE_CASE) to PatternMetadata(
                paymentMethod = "Bank SMS"
            ),
            Regex("spent (?:inr|rs\\.?)([\\d,]+(?:\\.\\d{1,2})?) on (.+)", RegexOption.IGNORE_CASE) to PatternMetadata(
                paymentMethod = "Card"
            )
        )
    }
}


