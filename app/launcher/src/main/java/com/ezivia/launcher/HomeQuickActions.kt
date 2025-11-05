package com.ezivia.launcher

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

import com.ezivia.launcher.R

/**
 * Describes the quick actions highlighted on the Ezivia home screen so they can be
 * rendered consistently in adapters and tested in isolation.
 */
data class HomeQuickAction(
    val type: HomeQuickActionType,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
)

enum class HomeQuickActionType {
    CALL,
    VIDEO_CALL,
    MESSAGE,
    PHOTOS,
    REMINDERS,
    SOS,
}

object HomeQuickActions {

    /** Returns the default set of quick actions for the home experience. */
    fun defaultActions(): List<HomeQuickAction> = listOf(
        HomeQuickAction(
            type = HomeQuickActionType.CALL,
            iconRes = R.drawable.ic_action_call,
            titleRes = R.string.quick_action_call_title,
            descriptionRes = R.string.quick_action_call_description,
        ),
        HomeQuickAction(
            type = HomeQuickActionType.VIDEO_CALL,
            iconRes = R.drawable.ic_action_video_call,
            titleRes = R.string.quick_action_video_title,
            descriptionRes = R.string.quick_action_video_description,
        ),
        HomeQuickAction(
            type = HomeQuickActionType.MESSAGE,
            iconRes = R.drawable.ic_action_message,
            titleRes = R.string.quick_action_message_title,
            descriptionRes = R.string.quick_action_message_description,
        ),
        HomeQuickAction(
            type = HomeQuickActionType.PHOTOS,
            iconRes = R.drawable.ic_action_photos,
            titleRes = R.string.quick_action_photos_title,
            descriptionRes = R.string.quick_action_photos_description,
        ),
        HomeQuickAction(
            type = HomeQuickActionType.REMINDERS,
            iconRes = R.drawable.ic_action_reminders,
            titleRes = R.string.quick_action_reminders_title,
            descriptionRes = R.string.quick_action_reminders_description,
        ),
        HomeQuickAction(
            type = HomeQuickActionType.SOS,
            iconRes = R.drawable.ic_action_sos,
            titleRes = R.string.quick_action_sos_title,
            descriptionRes = R.string.quick_action_sos_description,
        ),
    )
}
