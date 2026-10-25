package com.equilibra.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class. @HiltAndroidApp makes Hilt generate the app-wide
 * dependency container, which lives as long as the app process.
 */
@HiltAndroidApp
class EquilibraApp : Application()
