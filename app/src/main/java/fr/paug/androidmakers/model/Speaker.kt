package fr.paug.androidmakers.model

import android.text.TextUtils

class Speaker(val name: String,
              val bio: String,
              val company: String,
              val surname: String,
              val thumbnailUrl: String,
              isRockStar: String?,
              val socialNetworkHandles: List<SocialNetworkHandle>?,
              val ribbonList: List<Ribbon>?) {

    val isRockStar: Boolean = java.lang.Boolean.parseBoolean(isRockStar)

    val fullNameAndCompany: String
        get() = "${this.name} ${this.surname}${if (TextUtils.isEmpty(company)) "" else ", " + this.company}"

    /**
     * Gets the main ribbon.
     * The main ribbon is the first ribbon of the list.
     *
     * @return the main ribbon, or null if the ribbon list is empty or null.
     */
    val mainRibbon: Ribbon?
        get() = if (ribbonList != null && !ribbonList.isEmpty()) {
            ribbonList[0]
        } else null

}