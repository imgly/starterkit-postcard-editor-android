@file:Suppress("UnusedReceiverParameter")

package ly.img.editor.configuration.postcard.callback

import androidx.core.net.toUri
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ly.img.editor.configuration.postcard.PostcardConfigurationBuilder
import ly.img.editor.core.library.data.AssetSourceType
import ly.img.editor.core.library.data.SystemGalleryAssetSource
import ly.img.editor.core.library.data.SystemGalleryPermission
import ly.img.editor.core.library.data.TextAssetSource
import ly.img.editor.core.library.data.TypefaceProvider
import ly.img.engine.DefaultAssetSource
import ly.img.engine.DemoAssetSource
import ly.img.engine.GlobalScope
import ly.img.engine.populateAssetSource

suspend fun PostcardConfigurationBuilder.onCreate(
    preCreateScene: suspend PostcardConfigurationBuilder.() -> Unit = {
        onPreCreateScene()
    },
    createScene: suspend PostcardConfigurationBuilder.() -> Unit = {
        onCreateScene()
    },
    loadAssetSources: suspend PostcardConfigurationBuilder.() -> Unit = {
        onLoadAssetSources()
    },
    postCreateScene: suspend PostcardConfigurationBuilder.() -> Unit = {
        onPostCreateScene()
    },
    finally: suspend PostcardConfigurationBuilder.() -> Unit = {
        onCreateFinally()
    },
) {
    try {
        preCreateScene()
        createScene()
        loadAssetSources()
        postCreateScene()
    } finally {
        finally()
    }
}

fun PostcardConfigurationBuilder.onPreCreateScene() {
    showLoading = true
    editorContext.engine.editor.setGlobalScope("editor/add", GlobalScope.DEFER)
}

// highlight-starter-kit-postcard-on-create-scene
suspend fun PostcardConfigurationBuilder.onCreateScene() {
    getOrLoadScene(sceneUri = "file:///android_asset/scene/postcard.scene".toUri())
}
// highlight-starter-kit-postcard-on-create-scene

// highlight-starter-kit-postcard-on-load-asset-sources
suspend fun PostcardConfigurationBuilder.onLoadAssetSources() {
    // Load asset sources in parallel from content.json files
    coroutineScope {
        listOf(
            DefaultAssetSource.STICKER.key to defaultAssetSourceBaseUri,
            DefaultAssetSource.VECTOR_PATH.key to defaultAssetSourceBaseUri,
            DefaultAssetSource.FILTER_LUT.key to defaultAssetSourceBaseUri,
            DefaultAssetSource.FILTER_DUO_TONE.key to defaultAssetSourceBaseUri,
            DefaultAssetSource.CROP_PRESETS.key to defaultAssetSourceBaseUri,
            DefaultAssetSource.EFFECT.key to defaultAssetSourceBaseUri,
            DefaultAssetSource.BLUR.key to defaultAssetSourceBaseUri,
            DefaultAssetSource.TYPEFACE.key to defaultAssetSourceBaseUri,
            DemoAssetSource.IMAGE.key to demoAssetSourceBaseUri,
            DemoAssetSource.TEXT_COMPONENTS.key to demoAssetSourceBaseUri,
        ).forEach { (assetSource, basePath) ->
            launch {
                editorContext.engine.populateAssetSource(
                    id = assetSource,
                    jsonUri = "$basePath/$assetSource/content.json".toUri(),
                    replaceBaseUri = basePath,
                )
            }
        }
    }

    // Load local asset sources
    editorContext.engine.asset.addLocalSource(
        sourceId = DemoAssetSource.IMAGE_UPLOAD.key,
        supportedMimeTypes = listOf(
            "image/jpeg",
            "image/png",
            "image/heic",
            "image/heif",
            "image/svg+xml",
            "image/gif",
            "image/bmp",
        ),
    )

    // Register gallery asset sources
    listOf(
        AssetSourceType.GalleryAllVisuals,
        AssetSourceType.GalleryImage,
        AssetSourceType.GalleryVideo,
    ).forEach { type ->
        editorContext.engine.asset.addSource(
            source = SystemGalleryAssetSource(
                context = editorContext.engine.applicationContext,
                type = type,
            ),
        )
    }
    SystemGalleryPermission.setMode(systemGalleryConfiguration)

    // Register text asset source
    TypefaceProvider().provideTypeface(
        engine = editorContext.engine,
        name = "Roboto",
    )?.let {
        val textAssetSource = TextAssetSource(engine = editorContext.engine, typeface = it)
        editorContext.engine.asset.addSource(textAssetSource)
    }
}
// highlight-starter-kit-postcard-on-load-asset-sources

fun PostcardConfigurationBuilder.onPostCreateScene() {
    // Do nothing
}

fun PostcardConfigurationBuilder.onCreateFinally() {
    showLoading = false
}
