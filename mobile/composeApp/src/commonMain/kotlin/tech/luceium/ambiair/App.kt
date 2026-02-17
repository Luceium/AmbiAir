package tech.luceium.ambiair

import ContentWithMessageBar
import MessageBarState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.icerock.moko.biometry.compose.BindBiometryAuthenticatorEffect
import dev.icerock.moko.biometry.compose.rememberBiometryAuthenticatorFactory
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.launch
import rememberMessageBarState
import tech.luceium.ambiair.data.Config
import tech.luceium.ambiair.data.FirebaseRepository
import tech.luceium.ambiair.settings.AddDeviceScreen
import tech.luceium.ambiair.settings.BuildHomeScreen
import tech.luceium.ambiair.settings.HomeLocationScreen
import tech.luceium.ambiair.settings.PlaceSensorsScreen
import tech.luceium.ambiair.settings.PlaceWindowSensorsScreen
import tech.luceium.ambiair.settings.PlaceWindowsScreen
import tech.luceium.ambiair.ui.theme.AmbiAirTheme

@Composable
fun App() {
    wrappers { messageBarState ->
        val scope = rememberCoroutineScope()
        val auth = remember { Firebase.auth }


        val permissionsControllerFactory = rememberPermissionsControllerFactory()
        val permissionsController =
            remember(permissionsControllerFactory) { permissionsControllerFactory.createPermissionsController() }
        BindEffect(permissionsController)

        // Biometric Setup
        val biometryFactory = rememberBiometryAuthenticatorFactory()
        val biometryAuthenticator = remember { biometryFactory.createBiometryAuthenticator() }

        // Bind biometry authenticator to lifecycle
        BindBiometryAuthenticatorEffect(biometryAuthenticator)

        // State for Biometric Check Flow
        var biometricCheckState by remember { mutableStateOf(BiometricCheckState.IDLE) }

        // Config state
        var config by remember { mutableStateOf<Config?>(null) }
        var loadingConfig by remember { mutableStateOf(false) }
        var inProgressConfig by remember { mutableStateOf<Config?>(null) }

        // Combined Biometric Check and Config Load Logic
        LaunchedEffect(Unit) {
            val user = auth.currentUser
            if (user != null && !user.isAnonymous) {
                Logger.i(
                    "App launch: Found logged-in user (${user.uid}). Checking biometric status.",
                    null,
                    "luceium.ambiair.app.launchEffect"
                )

                // --- Check biometry status first ---
                val isBiometryAvailable = try {
                    // Use isBiometricAvailable as per the provided documentation
                    biometryAuthenticator.isBiometricAvailable()
                } catch (e: Exception) {
                    Logger.e(
                        e,
                        "luceium.ambiair.app.launchEffect"
                    ) { "Error checking biometry availability. Assuming unavailable." }
                    false // Treat error as unavailable
                }
                // --- End Check ---

                // Proceed with authentication prompt ONLY if biometrics are available
                if (isBiometryAvailable) {
                    Logger.i(
                        "Biometrics available. Starting authentication check.",
                        null,
                        "luceium.ambiair.app.launchEffect"
                    )
                    biometricCheckState = BiometricCheckState.CHECKING
                    try {
                        // Use checkBiometryAuthentication as per the provided documentation
                        val success = biometryAuthenticator.checkBiometryAuthentication(
                            requestTitle = "Verify Identity".desc(),
                            requestReason = "Please authenticate to continue.".desc(),
                            failureButtonText = "Use Password".desc(),
                            allowDeviceCredentials = true // Or false depending on your preference
                        )
                        if (success) {
                            Logger.i(
                                "Biometric check successful. Config will be loaded by the other effect.",
                                null,
                                "luceium.ambiair.app.launchEffect"
                            )
                            biometricCheckState = BiometricCheckState.SUCCESS
                        } else {
                            Logger.w(
                                "Biometric check failed by user.",
                                null,
                                "luceium.ambiair.app.launchEffect"
                            )
                            biometricCheckState = BiometricCheckState.FAILURE
                            // TODO: Remove if assumption holds true
                            require(!loadingConfig) { "Loading config should not be true when biometric check fails." }
                        }
                    } catch (bioError: Exception) {
                        Logger.e(
                            bioError,
                            "luceium.ambiair.app.launchEffect"
                        ) { "Biometric authentication error." }
                        biometricCheckState = BiometricCheckState.FAILURE // Treat errors as failure
                        // TODO: Remove if assumption holds true
                        require(!loadingConfig) { "Loading config should not be true when biometric check fails." }
                    }
                } else {
                    // Biometrics not available, not enrolled, locked out, or error occurred
                    Logger.i(
                        "Biometrics status is not available. Skipping authentication prompt.",
                        null,
                        "luceium.ambiair.app.launchEffect"
                    )
                    biometricCheckState = BiometricCheckState.NOT_REQUIRED
                }
            } else {
                Logger.i(
                    "App launch: No logged-in user or user is anonymous. Skipping biometric check.",
                    null,
                    "luceium.ambiair.app.launchEffect"
                )
                // TODO: Find a better way to handle this
                biometricCheckState = BiometricCheckState.NOT_REQUIRED // Skip the check
                // TODO: Remove if assumption holds true
                require(!loadingConfig) { "Loading config should not be true when user is null or anonymous." }
            }
        }

        // --- THIS EFFECT HANDLES CONFIG LOADING WHENEVER USER CHANGES (or after successful biometric check) ---
        LaunchedEffect(auth.currentUser?.uid, biometricCheckState) {
            // Load config only if biometric check passed/skipped, AND we have a user ID
            require(!loadingConfig) {
                // This is technically possible but highly unlikely for a human
                "Loading config should not be true when user changes or biometric check is in progress."
            }
            val userId = auth.currentUser?.uid
            if (userId != null &&
                (biometricCheckState == BiometricCheckState.SUCCESS || biometricCheckState == BiometricCheckState.NOT_REQUIRED)
            ) {
                Logger.i(
                    "User changed/Biometrics passed, User ID: $userId, Biometrics ${biometricCheckState}. Loading config...",
                    null,
                    "luceium.ambiair.app.launchEffect"
                )
                loadingConfig = true
                try {
                    config = FirebaseRepository.getConfig(userId)
                    Logger.i("Config loaded: $config", null, "luceium.ambiair.app.launchEffect")
                } catch (e: Exception) {
                    Logger.e(
                        e,
                        "luceium.ambiair.app.launchEffect"
                    ) { "Failed to load config for user $userId" }
                    messageBarState.addError(e)
                    config = null // Ensure config is null on error
                }
                loadingConfig = false
            }
        }

        // === UI Rendering Logic ===

        // 1. Show Biometric Checking UI if in progress
        if (biometricCheckState == BiometricCheckState.CHECKING) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.size(16.dp))
                Text("Checking Biometrics...")
            }
            return@wrappers // Stop rendering further UI
        }

        // 2. Show Loading Config UI
        // Show if loadingConfig is true, UNLESS biometric check has already failed (which leads to AUTH anyway)
        // TODO: Handle loading state in UI screens by displaying skeletons or placeholders
        if (loadingConfig) {
            // TODO: Remove if assumption holds true
            require(biometricCheckState == BiometricCheckState.SUCCESS || biometricCheckState == BiometricCheckState.NOT_REQUIRED) {
                "Loading config until user passes biometrics."
            }
            LoadingScreen()
            return@wrappers // Prevent NavHost rendering until loading is false
        }
        // TODO: Remove if assumption holds true
        require(!loadingConfig)

        // 3. Setup Navigation (only gets here if biometrics done/failed AND config loading done/not needed)
        val navController = rememberNavController()
        NavHost(navController, startDestination = Routes.HOME.name) {
            composable(Routes.AUTH.name) {
                AuthScreen(
                    scope,
                    auth,
                    toastError = { messageBarState.addError(it) },
                    onAuthFinished = {
                        Logger.i(
                            "User logged in via AuthScreen, navigating to HOME route trigger.",
                            null,
                            "luceium.ambiair.app.nav"
                        )
                        // Let the HOME route's logic handle config loading and final destination
                        navController.navigate(Routes.HOME.name) {
                            popUpTo(Routes.AUTH.name) {
                                inclusive = true
                            }
                        }
                    })
            }
            composable(Routes.HOME.name) {
                val user = auth.currentUser
                if (biometricCheckState == BiometricCheckState.IDLE) {
                    // Show loading screen while waiting for biometric check
                    LoadingScreen()
                    return@composable // Stop rendering further UI
                }
                // Condition 1: Navigate to AUTH if user is null OR if biometric check failed
                if (user == null || biometricCheckState == BiometricCheckState.FAILURE) {
                    scope.launch { // Ensure navigation happens in composition phase
                        navController.navigate(Routes.AUTH.name) {
                            popUpTo(Routes.HOME.name) {
                                inclusive = true
                            }
                        }
                        Logger.i(
                            "User null or biometric failed, navigating to AUTH",
                            null,
                            "luceium.ambiair.app.nav"
                        )
                    }
                }
                // Condition 2: Navigate to FIRST_SETUP if user doesn't have a config
                else if (config == null) {
                    Logger.i(
                        "Config loaded and is null, navigating to first setup",
                        null,
                        "luceium.ambiair.app.nav"
                    )
                    inProgressConfig = Config()
                    Logger.i(
                        "Creating default config for setup: $inProgressConfig",
                        null,
                        "luceium.ambiair.app.nav"
                    )
                    scope.launch { // Ensure navigation happens in composition phase
                        navController.navigate(Routes.FIRST_SETUP.name) {
                            // Prevent multiple rapid navigations if recompositions occur
                            launchSingleTop = true
                        }
                    }
                } else if (biometricCheckState == BiometricCheckState.IDLE) {
                    Logger.i(
                        "Biometric check is idle while attempting to load home, do nothing and wait",
                        null,
                        "luceium.ambiair.app.nav"
                    )
                }
                // Condition 3: Show HomeScreen if user exists, biometrics OK, config loaded (or user is anon)
                else { // User exists (and passed/skipped biometrics), config loaded (or user is anon)
                    HomeScreen(
                        user = user,
                        scope = scope,
                        signOut = {
                            scope.launch {
                                if (!user.isAnonymous) auth.signOut()
                                biometricCheckState = BiometricCheckState.IDLE // Reset state
                                navController.navigate(Routes.AUTH.name) {
                                    popUpTo(Routes.HOME.name) {
                                        inclusive = true
                                    }
                                }
                            }
                        },
                        config = config!!,
                        goToConfig = {
                            inProgressConfig = config?.deepCopy() ?: Config()
                            navController.navigate(Routes.CONFIG.name)
                        },
                        refreshHome = {
                            navController.navigate(Routes.HOME.name) {
                                popUpTo(Routes.HOME.name) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        toast = { messageBarState.addError(it) },
                    )
                }
            }
            composable(Routes.FIRST_SETUP.name) {
                WelcomeScreen(
                    onNext = {
                        inProgressConfig = config?.deepCopy() ?: Config()
                        navController.navigate(Routes.CONFIG.name)
                    },
                    onDone = { _config: Config ->
                        config = _config
                        FirebaseRepository.setConfig(
                            auth.currentUser!!.uid,
                            _config
                        )
                        navController.navigate(Routes.HOME.name) {
                            // Pop up to HOME and clear the back stack on the way
                            popUpTo(Routes.HOME.name)
                            // Block back stack entry creation (causing [HOME, HOME])
                            launchSingleTop = true
                        }
                        Logger.i("Finished config, navigating to home...\nPopping back stack\nBack stack: ${
                            navController
                                .currentBackStack.value
                                .map { it.destination.route }
                                .joinToString(", ")
                        }", null, "luceium.ambiair.app.config")
                    },
                    scope
                )
            }
            navigation(
                startDestination = ConfigRoutes.ADD_DEVICE.name,
                route = Routes.CONFIG.name
            ) {
                composable(ConfigRoutes.ADD_DEVICE.name) {
                    AddDeviceScreen(
                        inProgressConfig!!,
                        onNext = { navController.navigate(ConfigRoutes.BUILD_HOME.name) },
                    )
                }
                composable(ConfigRoutes.BUILD_HOME.name) {
                    BuildHomeScreen(
                        inProgressConfig!!,
                        onNext = { navController.navigate(ConfigRoutes.PLACE_WINDOWS.name) }
                    )
                }
                composable(ConfigRoutes.PLACE_WINDOWS.name) {
                    PlaceWindowsScreen(
                        inProgressConfig!!,
                        onNext = { navController.navigate(ConfigRoutes.PLACE_WINDOW_SENSORS.name) }
                    )
                }
                composable(ConfigRoutes.PLACE_WINDOW_SENSORS.name) {
                    PlaceWindowSensorsScreen(
                        inProgressConfig!!,
                        onNext = { navController.navigate(ConfigRoutes.PLACE_DEVICES.name) }
                    )
                }
                composable(ConfigRoutes.PLACE_DEVICES.name) {
                    PlaceSensorsScreen(
                        inProgressConfig!!,
                        onNext = { navController.navigate(ConfigRoutes.HOME_LOCATION.name) }
                    )
                }
                composable(ConfigRoutes.HOME_LOCATION.name) {
                    HomeLocationScreen(
                        inProgressConfig!!,
                        scope,
                        onDone = {
                            scope.launch {
                                config = inProgressConfig
                                FirebaseRepository.setConfig(
                                    auth.currentUser!!.uid,
                                    config!!
                                )
                            }
                            navController.navigate(Routes.HOME.name) {
                                // Pop up to HOME and clear the back stack on the way
                                popUpTo(Routes.HOME.name)
                                // Block back stack entry creation (causing [HOME, HOME])
                                launchSingleTop = true
                            }
                            Logger.i("Finished config, navigating to home...\nPopping back stack\nBack stack: ${
                                navController
                                    .currentBackStack.value
                                    .map { it.destination.route }
                                    .joinToString(", ")
                            }", null, "luceium.ambiair.app.config")
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun wrappers(content: @Composable (messageBarState: MessageBarState) -> Unit) {
    AmbiAirTheme {
        val messageBarState = rememberMessageBarState()
        ContentWithMessageBar(messageBarState = messageBarState) {
            content(messageBarState)
        }
    }
}

// ROUTES
enum class Routes(val title: String) {
    AUTH("Auth"),
    HOME("Home"),
    FIRST_SETUP("First Time Config"),
    CONFIG("Config"), // Nav Subgraph
}

enum class ConfigRoutes(title: String) {
    ADD_DEVICE("Add Device"),
    BUILD_HOME("Build Home"),
    PLACE_WINDOWS("Window Placement"),
    PLACE_WINDOW_SENSORS("Place Window Sensors"),
    PLACE_DEVICES("Place Sensors"),
    HOME_LOCATION("Home Location"),
}

// Biometric Check State Enum
private enum class BiometricCheckState {
    IDLE, CHECKING, SUCCESS, FAILURE, NOT_REQUIRED
}
