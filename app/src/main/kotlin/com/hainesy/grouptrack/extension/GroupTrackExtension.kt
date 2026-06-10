package com.hainesy.grouptrack.extension

import android.util.Log
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension

/**
 * GroupTrack Karoo extension entry point.
 *
 * Registered in AndroidManifest.xml via the `io.hammerhead.karooext.KAROO_EXTENSION`
 * intent filter. The extension id ("grouptrack") must match `id` in extension_info.xml.
 *
 * Stage 1 only declares the "Gap to Last" data type. Position publishing (Stage 2),
 * the gap engine (Stage 3) and mechanical alerts (Stage 4) will hang off this class.
 */
class GroupTrackExtension : KarooExtension(EXTENSION_ID, VERSION) {

    private lateinit var karooSystem: KarooSystemService

    override val types: List<DataTypeImpl> by lazy {
        listOf(GapToLastDataType(extension))
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "GroupTrack extension created (v$VERSION)")
        karooSystem = KarooSystemService(applicationContext)
        karooSystem.connect { connected ->
            Log.i(TAG, "Karoo system connected=$connected")
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "GroupTrack extension destroyed")
        if (::karooSystem.isInitialized) {
            karooSystem.disconnect()
        }
        super.onDestroy()
    }

    companion object {
        const val EXTENSION_ID = "grouptrack"
        const val VERSION = "0.1.0"
        private const val TAG = "GroupTrack/Ext"
    }
}
