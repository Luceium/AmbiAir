package tech.luceium.ambiair.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.components.WindowGridCanvas
import tech.luceium.ambiair.ui.theme.AmbiAirTypography

@Composable
fun PlaceWindowsScreen(
    config: Config,
    onNext: () -> Unit
) {
    val homeLayout = remember { config.homeLayout }
    val horizontalWindowLayout = remember { config.horizontalWindowLayout.toMutableStateList() }
    val verticalWindowLayout = remember { config.verticalWindowLayout.toMutableStateList() }

    val rows = 5
    val cols = 5
    val cellSizeDp = 60.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Text(
                "Set up",
                style = AmbiAirTypography().displayLarge,
                modifier = Modifier.padding(top=32.dp, bottom = 24.dp)
            )
            Text(
                "Place Windows\nClick to place",
                style = AmbiAirTypography().titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            WindowGridCanvas(
                homeLayout = homeLayout,
                horizontalWindowLayout = horizontalWindowLayout,
                verticalWindowLayout = verticalWindowLayout,
                rows = rows,
                cols = cols,
                cellSizeDp = cellSizeDp
            )
        }

        Button(
            onClick = {
                // Update the original config with our changes
                config.horizontalWindowLayout.clear()
                config.horizontalWindowLayout.addAll(horizontalWindowLayout)

                config.verticalWindowLayout.clear()
                config.verticalWindowLayout.addAll(verticalWindowLayout)

                // Now navigate to next screen
                onNext()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .width(140.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9DD88F) // Green color
                    ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Next", color = Color.Black, style = AmbiAirTypography().bodyLarge)
        }
    }
    // Linear Progress Indicator
    LinearProgressIndicator(
        progress = { 3f / 6f },
        modifier = Modifier.fillMaxWidth()
    )
}

