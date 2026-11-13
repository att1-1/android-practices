package ru.urfu.githubrepositories.data.local.profile

import android.content.Context
import androidx.datastore.dataStore

val Context.userProfileDataStore by dataStore(
    fileName = "user_profile.pb",
    serializer = UserProfileSerializer
)
