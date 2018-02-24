package fr.paug.androidmakers.model

import android.support.annotation.StringRes
import android.text.TextUtils

import fr.paug.androidmakers.R

class Session(val title: String, val description: String?, val language: String?,
              val speakers: IntArray?, val subtype: String?) {

    val languageName: Int
        @StringRes
        get() = Session.getLanguageFullName(this.language!!)

    companion object {
        @StringRes
        fun getLanguageFullName(abbreviatedVersion: String): Int {
            if (!TextUtils.isEmpty(abbreviatedVersion)) {
                if ("en".equals(abbreviatedVersion, ignoreCase = true)) {
                    return R.string.english
                } else if ("fr".equals(abbreviatedVersion, ignoreCase = true)) {
                    return R.string.french
                }
            }
            return 0
        }
    }

}