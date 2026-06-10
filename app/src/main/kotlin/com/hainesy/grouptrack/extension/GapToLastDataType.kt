package com.hainesy.grouptrack.extension

import android.util.Log
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * "Gap to Last" data field.
 *
 * Stage 1 placeholder: emits an incrementing value every two seconds so the
 * field is selectable in the Karoo data-field picker and visibly "live" on a
 * ride page. The real breadcrumb gap engine arrives in Stage 3 and will replace
 * the body of [startStream] with a computed gap in metres.
 */
class GapToLastDataType(extension: String) : DataTypeImpl(extension, TYPE_ID) {

    override fun startStream(emitter: Emitter<StreamState>) {
        Log.d(TAG, "startStream $TYPE_ID")
        val job = CoroutineScope(Dispatchers.IO).launch {
            var placeholder = 0.0
            while (isActive) {
                emitter.onNext(
                    StreamState.Streaming(
                        DataPoint(
                            dataTypeId = dataTypeId,
                            values = mapOf(DataType.Field.SINGLE to placeholder),
                        ),
                    ),
                )
                placeholder += 1.0
                delay(STREAM_INTERVAL_MS)
            }
        }
        emitter.setCancellable {
            Log.d(TAG, "stopStream $TYPE_ID")
            job.cancel()
        }
    }

    companion object {
        const val TYPE_ID = "gap-to-last"
        private const val TAG = "GroupTrack/GapToLast"
        private const val STREAM_INTERVAL_MS = 2_000L
    }
}
