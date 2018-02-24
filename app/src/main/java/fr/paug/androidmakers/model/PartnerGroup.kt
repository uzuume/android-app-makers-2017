package fr.paug.androidmakers.model

import android.support.annotation.StringRes
import android.text.TextUtils

import fr.paug.androidmakers.R

class PartnerGroup(val partnerType: PartnerType, val partnersList: List<Partners>) {

    enum class PartnerType {
        Unknown, GoldSponsor, SilverSponsor, OtherSponsor, Media, Location;

        val partnerTypeName: Int
            @StringRes
            get() {
                return when (this) {
                    GoldSponsor -> R.string.gold_sponsor
                    SilverSponsor -> R.string.silver_sponsor
                    OtherSponsor -> R.string.other_sponsor
                    Media -> R.string.media_sponsor
                    Location -> R.string.location_sponsor
                    else -> 0
                }
            }

        val partnerLogoSize: Int
            get() {
                return when (this) {
                    GoldSponsor -> 1
                    SilverSponsor, OtherSponsor, Location -> 2
                    Media -> 3
                    else -> 0
                }
            }
    }

    companion object {
        fun getPartnerTypeFromString(typeName: String): PartnerType {
            if (!TextUtils.isEmpty(typeName)) {
                when {
                    "gold+ sponsor".equals(typeName, ignoreCase = true) -> return PartnerType.GoldSponsor
                    "silver sponsor".equals(typeName, ignoreCase = true) -> return PartnerType.SilverSponsor
                    "other sponsor".equals(typeName, ignoreCase = true) -> return PartnerType.OtherSponsor
                    "media".equals(typeName, ignoreCase = true) -> return PartnerType.Media
                    "location".equals(typeName, ignoreCase = true) -> return PartnerType.Location
                    else -> {
                    }
                }
            }
            return PartnerType.Unknown
        }
    }

}