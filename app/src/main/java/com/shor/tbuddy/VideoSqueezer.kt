package com.shor.tbuddy

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import java.io.File

/**
 * Opt-in required for Media3 Transformer experimental APIs.
 * Annotations must follow the package declaration.
 */
@OptIn(UnstableApi::class)
class VideoSqueezer(private val context: Context) {

    fun squeeze(uri: Uri, onComplete: (File) -> Unit, onError: (Exception) -> Unit) {
        SlopLogger.info("SQUEEZER: Received URI: $uri. Preparing hardware crush...")

        // Create a unique temporary file in the cache directory
        val outputFile = File(context.cacheDir, "squeezed_${System.currentTimeMillis()}.mp4")

        val transformer = Transformer.Builder(context)
            .setTransformationRequest(
                TransformationRequest.Builder()
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .build()
            )
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (outputFile.exists() && outputFile.length() > 0) {
                        SlopLogger.success("SQUEEZER: Success. File Size: ${outputFile.length() / 1024} KB")
                        onComplete(outputFile)
                    } else {
                        val error = Exception("SQUEEZER_FAIL: Output file is empty (0KB).")
                        SlopLogger.error("SQUEEZER: Hardware produced a ghost file.", error)
                        onError(error)
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    SlopLogger.error("SQUEEZER: Transformation Error", exportException)
                    onError(exportException)
                }
            })
            .build()

        val mediaItem = MediaItem.fromUri(uri)
        // Optimization: Removing audio significantly reduces payload size for Gemini
        val editedMediaItem = EditedMediaItem.Builder(mediaItem).setRemoveAudio(true).build()

        try {
            transformer.start(editedMediaItem, outputFile.absolutePath)
        } catch (e: Exception) {
            SlopLogger.error("SQUEEZER: Failed to start transformer", e)
            onError(e)
        }
    }
}