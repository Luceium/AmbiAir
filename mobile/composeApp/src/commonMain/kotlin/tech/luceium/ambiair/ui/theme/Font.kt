package tech.luceium.ambiair.ui.theme

import ambiair_mobile.composeapp.generated.resources.Res
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import org.jetbrains.compose.resources.Font
import ambiair_mobile.composeapp.generated.resources.josefinsans_extralight
import ambiair_mobile.composeapp.generated.resources.josefinsans_extralightitalic
import ambiair_mobile.composeapp.generated.resources.josefinsans_light
import ambiair_mobile.composeapp.generated.resources.josefinsans_lightitalic
import ambiair_mobile.composeapp.generated.resources.josefinsans_regular
import ambiair_mobile.composeapp.generated.resources.josefinsans_italic
import ambiair_mobile.composeapp.generated.resources.josefinsans_medium
import ambiair_mobile.composeapp.generated.resources.josefinsans_mediumitalic
import ambiair_mobile.composeapp.generated.resources.josefinsans_semibold
import ambiair_mobile.composeapp.generated.resources.josefinsans_semibolditalic
import ambiair_mobile.composeapp.generated.resources.josefinsans_bold
import ambiair_mobile.composeapp.generated.resources.josefinsans_bolditalic
import ambiair_mobile.composeapp.generated.resources.museomoderno_extralight
import ambiair_mobile.composeapp.generated.resources.museomoderno_extralightitalic
import ambiair_mobile.composeapp.generated.resources.museomoderno_light
import ambiair_mobile.composeapp.generated.resources.museomoderno_lightitalic
import ambiair_mobile.composeapp.generated.resources.museomoderno_regular
import ambiair_mobile.composeapp.generated.resources.museomoderno_italic
import ambiair_mobile.composeapp.generated.resources.museomoderno_medium
import ambiair_mobile.composeapp.generated.resources.museomoderno_mediumitalic
import ambiair_mobile.composeapp.generated.resources.museomoderno_semibold
import ambiair_mobile.composeapp.generated.resources.museomoderno_semibolditalic
import ambiair_mobile.composeapp.generated.resources.museomoderno_bold
import ambiair_mobile.composeapp.generated.resources.museomoderno_bolditalic
import ambiair_mobile.composeapp.generated.resources.museomoderno_extrabold
import ambiair_mobile.composeapp.generated.resources.museomoderno_extrabolditalic
import ambiair_mobile.composeapp.generated.resources.museomoderno_black
import ambiair_mobile.composeapp.generated.resources.museomoderno_blackitalic
import androidx.compose.ui.text.font.FontWeight

object AppFonts {
    @Composable
    @OptIn(ExperimentalTextApi::class)
    fun museoModerno(): FontFamily = FontFamily(
        Font(Res.font.museomoderno_extralight, FontWeight.ExtraLight),
        Font(Res.font.museomoderno_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.museomoderno_light, FontWeight.Light),
        Font(Res.font.museomoderno_lightitalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.museomoderno_regular, FontWeight.Normal),
        Font(Res.font.museomoderno_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.museomoderno_medium, FontWeight.Medium),
        Font(Res.font.museomoderno_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.museomoderno_semibold, FontWeight.SemiBold),
        Font(Res.font.museomoderno_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.museomoderno_bold, FontWeight.Bold),
        Font(Res.font.museomoderno_bolditalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.museomoderno_extrabold, FontWeight.ExtraBold),
        Font(Res.font.museomoderno_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(Res.font.museomoderno_black, FontWeight.Black),
        Font(Res.font.museomoderno_blackitalic, FontWeight.Black, FontStyle.Italic)
    )

    @Composable
    @OptIn(ExperimentalTextApi::class)
    fun josefineSans(): FontFamily  = FontFamily(
        Font(Res.font.josefinsans_extralight, FontWeight.ExtraLight),
        Font(Res.font.josefinsans_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.josefinsans_light, FontWeight.Light),
        Font(Res.font.josefinsans_lightitalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.josefinsans_regular, FontWeight.Normal),
        Font(Res.font.josefinsans_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.josefinsans_medium, FontWeight.Medium),
        Font(Res.font.josefinsans_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.josefinsans_semibold, FontWeight.SemiBold),
        Font(Res.font.josefinsans_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.josefinsans_bold, FontWeight.Bold),
        Font(Res.font.josefinsans_bolditalic, FontWeight.Bold, FontStyle.Italic)
    )
}