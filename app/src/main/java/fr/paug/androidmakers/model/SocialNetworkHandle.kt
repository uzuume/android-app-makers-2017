package fr.paug.androidmakers.model

import android.support.annotation.DrawableRes
import android.text.TextUtils

import fr.paug.androidmakers.R

class SocialNetworkHandle(name: String, val link: String) {

    val networkType: SocialNetworkType

    enum class SocialNetworkType {
        Unknown, Twitter, GooglePlus, Facebook, Website, Github;

        val socialNetworkIcon: Int
            @DrawableRes
            get() {
                return when (this) {
                    Twitter -> R.drawable.ic_twitter_logo_white_on_blue
                    GooglePlus -> R.drawable.google_plus
                    Facebook -> R.drawable.fb_logo_blue
                    Github -> R.drawable.github_mark
                    Website -> R.drawable.ic_public_black_24dp
                    else -> R.drawable.ic_public_black_24dp
                }
            }
    }

    init {
        this.networkType = getSocialNetworkType(name)
    }

    private fun getSocialNetworkType(networkName: String): SocialNetworkType {
        if (!TextUtils.isEmpty(networkName)) {
            when {
                networkName.equals("twitter", ignoreCase = true) -> return SocialNetworkType.Twitter
                networkName.equals("google-plus", ignoreCase = true) -> return SocialNetworkType.GooglePlus
                networkName.equals("github", ignoreCase = true) -> return SocialNetworkType.Github
                networkName.equals("site", ignoreCase = true) -> return SocialNetworkType.Website
                networkName.equals("facebook", ignoreCase = true) -> return SocialNetworkType.Facebook
                else -> {
                }
            }
        }
        return SocialNetworkType.Unknown
    }
}