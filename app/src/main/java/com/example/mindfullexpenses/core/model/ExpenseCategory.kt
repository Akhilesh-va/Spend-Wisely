package com.example.mindfullexpenses.core.model

enum class ExpenseCategory(val displayName: String) {
    FOOD_AND_DINING("Food & Dining"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    BILLS_AND_UTILITIES("Bills & Utilities"),
    HEALTH("Health"),
    EDUCATION("Education"),
    GROCERIES("Groceries"),
    TRAVEL("Travel"),
    SUBSCRIPTIONS("Subscriptions"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): ExpenseCategory {
            return entries.firstOrNull {
                it.displayName.equals(name, ignoreCase = true)
            } ?: OTHER
        }
    }
}


