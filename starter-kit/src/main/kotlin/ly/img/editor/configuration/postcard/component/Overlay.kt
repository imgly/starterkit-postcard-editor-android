package ly.img.editor.configuration.postcard.component

import androidx.compose.runtime.Composable
import ly.img.editor.configuration.postcard.PostcardConfigurationBuilder
import ly.img.editor.core.component.EditorComponent
import ly.img.editor.core.component.remember

@Composable
fun PostcardConfigurationBuilder.rememberOverlay() = EditorComponent.remember {
    decoration = { Overlay() }
}
