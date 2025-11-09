package com.example.mindfullexpenses.core.autotrack

object NotificationSources {
    val paymentPackages: Set<String> = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.amazon.mShop.android.shopping",
        "in.amazon.mShop.android.shopping",
        "com.freecharge.android",
        "com.mobikwik_new"
    )

    val smsPackages: Set<String> = setOf(
        "com.google.android.apps.messaging",
        "com.android.messaging",
        "com.samsung.android.messaging",
        "com.miui.mms",
        "com.oneplus.mms",
        "com.motorola.messaging"
    )

    val supportedPackages: Set<String> = paymentPackages + smsPackages
}
