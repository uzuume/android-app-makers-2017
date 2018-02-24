package fr.paug.androidmakers.model

import android.support.annotation.DrawableRes
import android.text.TextUtils

import fr.paug.androidmakers.R

class Ribbon(ribbonName: String, val title: String, val link: String) {

    val ribbonType: RibbonType

    enum class RibbonType {
        NONE, GDE, GDG;

        val ribbonIcon: Int
            @DrawableRes
            get() {
                when (this) {
                    GDE -> return R.drawable.gde_logo
                    GDG -> return R.drawable.gdg_logo
                    else -> return -1
                }
            }

        val badgeIcon: Int
            @DrawableRes
            get() {
                when (this) {
                    GDE -> return R.drawable.gde_badge
                    GDG -> return R.drawable.gdg_badge
                    else -> return -1
                }
            }
    }

    init {
        this.ribbonType = getRibbonType(ribbonName)
    }

    private fun getRibbonType(ribbonName: String): RibbonType {
        if (!TextUtils.isEmpty(ribbonName)) {
            if (ribbonName.equals("GDE", ignoreCase = true)) {
                return RibbonType.GDE
            } else if (ribbonName.equals("GDG", ignoreCase = true)) {
                return RibbonType.GDG
            }
        }
        return RibbonType.NONE
    }

}