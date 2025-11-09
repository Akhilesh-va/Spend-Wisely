package com.example.mindfullexpenses.core.autotrack.parser

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.toBigDecimalOrNull

@Singleton
class NotificationParser @Inject constructor() {

    fun parse(
        packageName: String,
        title: CharSequence?,
        text: CharSequence?,
        timestamp: Long
    ): ParsedExpense? {
        val safeText = text?.toString() ?: return null
        val patterns = patternsByPackage[packageName] ?: return null

        for (pattern in patterns) {
            val match = pattern.find(safeText) ?: continue
            val amountMinor = match.groupValues.getOrNull(1)?.toAmountMinor() ?: continue
            val merchantRaw = match.groupValues.getOrNull(2)?.trim().orEmpty()
            val cleanMerchant = MerchantCleaner.clean(merchantRaw)
            val isCredit = creditKeywords.any { safeText.contains(it, ignoreCase = true) }

            return ParsedExpense(
                amountMinor = if (isCredit) -amountMinor else amountMinor,
                rawMerchant = merchantRaw,
                cleanMerchant = cleanMerchant,
                paymentMethod = appName(packageName),
                timestamp = Instant.ofEpochMilli(timestamp),
                isCredit = isCredit
            )
        }

        return null
    }

    private fun String.toAmountMinor(): Long? {
        val sanitized = replace(",", "")
            .replace("₹", "")
            .replace("Rs.", "")
            .replace("Rs", "")
            .trim()
        return sanitized.toBigDecimalOrNull()?.multiply(HUNDRED)?.toLong()
    }

    private fun appName(packageName: String): String = appNameOverrides[packageName] ?: packageName

    companion object {
        private val HUNDRED = java.math.BigDecimal(100)

        private val creditKeywords = listOf("credited", "refund", "received")

        private val appNameOverrides = mapOf(
            "com.phonepe.app" to "PhonePe",
            "com.google.android.apps.nbu.paisa.user" to "Google Pay",
            "net.one97.paytm" to "Paytm",
            "in.org.npci.upiapp" to "BHIM",
            "com.amazon.mShop.android.shopping" to "Amazon Pay",
            "com.flipkart.android" to "Flipkart",
            "com.paytm.android" to "Paytm"
        )

        private val patternsByPackage: Map<String, List<Regex>> = mapOf(
            "com.phonepe.app" to listOf(
                Regex("Payment of Rs.?([\\d,]+(?:\\.\\d{1,2})?) to (.+?) is successful", RegexOption.IGNORE_CASE),
                Regex("You paid Rs.?([\\d,]+(?:\\.\\d{1,2})?) to (.+)", RegexOption.IGNORE_CASE)
            ),
            "com.google.android.apps.nbu.paisa.user" to listOf(
                Regex("You paid ₹([\\d,]+(?:\\.\\d{1,2})?) to (.+)", RegexOption.IGNORE_CASE),
                Regex("Paid ₹([\\d,]+(?:\\.\\d{1,2})?) to (.+)", RegexOption.IGNORE_CASE)
            ),
            "net.one97.paytm" to listOf(
                Regex("Money sent to (.+?)\\. Rs.?([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
                Regex("Payment of Rs.?([\\d,]+(?:\\.\\d{1,2})?) made to (.+?) is successful", RegexOption.IGNORE_CASE)
            ),
            "com.phonepe.app.bank" to listOf(
                Regex("A/c .* debited for Rs.?([\\d,]+(?:\\.\\d{1,2})?) (?:to|for) (.+)", RegexOption.IGNORE_CASE)
            )
        )
    }
}

object MerchantCleaner {
    fun clean(value: String): String = value
        .lowercase()
        .replace("upi-", "")
        .replace(Regex("@[a-zA-Z0-9]+"), "")
        .replace(Regex("\\d{10}"), "")
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

data class ParsedExpense(
    val amountMinor: Long,
    val rawMerchant: String,
    val cleanMerchant: String,
    val paymentMethod: String,
    val timestamp: Instant,
    val isCredit: Boolean,
    val currencyCode: String = "INR"
)


