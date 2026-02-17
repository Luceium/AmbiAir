package tech.luceium.ambiair.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.ui.theme.AmbiAirTypography


@Composable
fun BuildHomeScreen(
    config: Config,
    onNext: () -> Unit
) {
    // Get reference for the home layout
    val homeLayout = remember { config.homeLayout }

    // Flatten for easier GridView
    val flattenedLayout = remember { mutableStateListOf<Boolean>().apply {
        homeLayout.forEach { cell ->
            add(cell) // Only get first boolean value
        }
    } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Set up",
                style = AmbiAirTypography().displayLarge,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )

            // Subtitle
            Text(
                text = "Draw Floor Plan\nClick to fill box",
                style = AmbiAirTypography().titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .height(375.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(25) { index ->
                    val isFilled = flattenedLayout[index]

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .background(
                                color = if (isFilled) Color(0xFF996633) else Color.White,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                flattenedLayout[index] = !isFilled // Toggle the box

                                // Update the original homeLayout
                                homeLayout[index] = !homeLayout[index]
                            }
                    )
                }
            }
        }

        // Next Button
        Button(
            onClick = onNext,
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
    LinearProgressIndicator(
        progress = { 2f / 6f },
        modifier = Modifier
            .fillMaxWidth()
    )
}
