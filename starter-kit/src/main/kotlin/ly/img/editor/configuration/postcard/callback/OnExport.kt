package ly.img.editor.configuration.postcard.callback

import kotlinx.coroutines.CancellationException
import ly.img.editor.configuration.postcard.PostcardConfigurationBuilder
import ly.img.engine.MimeType
import java.nio.ByteBuffer

suspend fun PostcardConfigurationBuilder.onExport(
    preExport: suspend PostcardConfigurationBuilder.() -> Unit = {
        onPreExport()
    },
    exportByteBuffer: suspend PostcardConfigurationBuilder.() -> ByteBuffer = {
        onExportByteBuffer()
    },
    postExport: suspend PostcardConfigurationBuilder.(ByteBuffer) -> Unit = {
        onPostExport(it)
    },
    error: suspend PostcardConfigurationBuilder.(Exception) -> Unit = {
        onExportError(it)
    },
    finally: suspend PostcardConfigurationBuilder.() -> Unit = {
        onExportFinally()
    },
) {
    try {
        preExport()
        val result = exportByteBuffer()
        postExport(result)
    } catch (exception: Exception) {
        error(exception)
    } finally {
        finally()
    }
}

fun PostcardConfigurationBuilder.onPreExport() {
    showLoading = true
}

suspend fun PostcardConfigurationBuilder.onExportByteBuffer(): ByteBuffer = export(
    block = requireNotNull(editorContext.engine.scene.get()),
    mimeType = MimeType.PDF,
)

suspend fun PostcardConfigurationBuilder.onPostExport(byteBuffer: ByteBuffer) {
    val file = writeToFile(byteBuffer = byteBuffer, mimeType = MimeType.PDF)
    shareFile(file = file, mimeType = MimeType.PDF)
}

fun PostcardConfigurationBuilder.onExportError(error: Exception) {
    if (error is CancellationException) {
        throw error
    }
    this.error = error
}

fun PostcardConfigurationBuilder.onExportFinally() {
    showLoading = false
}
