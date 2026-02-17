package tech.luceium.ambiair

import ambiair_mobile.composeapp.generated.resources.Res
import ambiair_mobile.composeapp.generated.resources.already_have_home_import_it
import ambiair_mobile.composeapp.generated.resources.lets_set_up_your_home
import ambiair_mobile.composeapp.generated.resources.next
import ambiair_mobile.composeapp.generated.resources.welcome_to
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.ui.theme.AmbiAirTypography
import tech.luceium.ambiair.ui.theme.extendedDark

@Composable
fun WelcomeScreen(onNext: () -> Unit, onDone: suspend (config: Config) -> Unit, scope: CoroutineScope) {

    val launcher = rememberFilePickerLauncher { file ->
        if (file != null) {
            scope.launch {
                val jsonString = file.readString()
                Logger.i("JSONSTRING")
                try {
                    val configObject = Json.decodeFromString<Config>(jsonString)
                    Logger.i("PP")
                    onDone(configObject)
                } catch (e: Exception) {
                    Logger.i("JSON not decoded", e)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val annotatedWelcomeString = buildAnnotatedString {
            append(stringResource(Res.string.welcome_to) + " ")
            withStyle(
                style = SpanStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            extendedDark.cold.color,
                            extendedDark.cool.color,
                            extendedDark.warm.color,
                            extendedDark.hot.color
                        )
                    ),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("ambiair")
            }
        }
        Text(
            text = annotatedWelcomeString,
            style = AmbiAirTypography().headlineLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 48.sp,
                lineHeight = 56.sp,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.padding(32.dp))
        Text(
            text = stringResource(Res.string.lets_set_up_your_home),
            style = AmbiAirTypography().headlineMedium.copy(
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onNext) {
            Text(stringResource(Res.string.next))
        }
        Spacer(modifier = Modifier.padding(8.dp))
        TextButton(onClick = {launcher.launch()}) {
            Text(stringResource(Res.string.already_have_home_import_it))
        }
    }
}