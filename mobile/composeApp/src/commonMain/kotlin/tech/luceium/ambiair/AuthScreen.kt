package tech.luceium.ambiair

import ambiair_mobile.composeapp.generated.resources.Res
import ambiair_mobile.composeapp.generated.resources.app_name
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tech.luceium.ambiair.ui.theme.AmbiAirTypography
import tech.luceium.ambiair.ui.theme.extendedDark
import dev.icerock.moko.biometry.compose.BiometryAuthenticatorFactory
import dev.icerock.moko.biometry.compose.BindBiometryAuthenticatorEffect
import dev.icerock.moko.biometry.compose.rememberBiometryAuthenticatorFactory
import androidx.compose.runtime.collectAsState

@Composable
fun AuthScreen(
    scope: CoroutineScope,
    auth: FirebaseAuth,
    toastError: (Exception) -> Unit,
    onAuthFinished: () -> Unit
) {
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var guestModeDialgOpen by remember { mutableStateOf(false) }
    var waitingForAuthResp by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }
    val validationErrors = validateAuthData(userEmail, userPassword)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 30.dp, vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        header()

        Spacer(modifier = Modifier.size(64.dp))

        Column {
            form(
                userEmail, { userEmail = it },
                userPassword, { userPassword = it }
            )
            formErrors(validationErrors, attemptedSubmit)

            authOptions(
                scope, auth, userEmail, userPassword,
                { guestModeDialgOpen = true },
                waitingForAuthResp, { waitingForAuthResp = it },
                validationErrors, toastError, onAuthFinished,
                { attemptedSubmit = true }
            )
        }

        guestModel(
            guestModeDialgOpen, { guestModeDialgOpen = false },
            scope, auth, { waitingForAuthResp = it }, onAuthFinished
        )
    }
}

@Composable
fun formErrors(validationErrors: List<String>, attemptedSubmit: Boolean) {
    if (attemptedSubmit) {
        for (error in validationErrors) {
            Text(
                text = error,
                style = AmbiAirTypography().bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

val emailRegex = Regex("^[A-Za-z]\\S*@.+\\.\\S+")
fun validateAuthData(userEmail: String, userPassword: String): List<String> {
    val validEmail = userEmail.isNotBlank() && emailRegex.matches(userEmail)
    val validPassLen = userPassword.length >= 8
    val validPassStrength = userPassword.any { it.isUpperCase() } &&
            userPassword.any { it.isLowerCase() } &&
            userPassword.any { it.isDigit() } &&
            userPassword.any { "!@#$%^&*()_+=-".contains(it) }

    val errors = mutableListOf<String>()
    if (!validEmail) errors.add("Invalid email")
    if (!validPassLen) errors.add("Password must be at least 8 characters long")
    if (!validPassStrength) errors.add("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (!@#$%^&*()-_=+)")
    return errors
}

@Composable
private fun header() {
    Text(
        text = stringResource(Res.string.app_name),
        style = AmbiAirTypography().headlineLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 80.sp,
            lineHeight = 102.sp,
            textAlign = TextAlign.Center,
            brush = Brush.linearGradient(
                colors = listOf(
                    extendedDark.cold.color,
                    extendedDark.cool.color,
                    extendedDark.warm.color,
                    extendedDark.hot.color
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
    )

    Text(
        text = "Save your wallet\nSave the planet",
        style = AmbiAirTypography().headlineLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
private fun form(
    userEmail: String,
    onUserEmailChange: (String) -> Unit,
    userPassword: String,
    onUserPasswordChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        userEmail,
        onValueChange = onUserEmailChange,
        label = { Text("Username") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        )
    )
    Spacer(Modifier.size(16.dp))
    PasswordField(
        userPassword,
        onUserPasswordChange,
        focusManager,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun authOptions(
    scope: CoroutineScope, auth: FirebaseAuth,
    userEmail: String, userPassword: String,
    openGuestModal: () -> Unit,
    waitingForAuthResp: Boolean, setWaitingForAuthResp: (Boolean) -> Unit,
    validationErrors: List<String>,
    toastError: (Exception) -> Unit,
    onAuthFinished: () -> Unit,
    setAttemptedSubmit: () -> Unit
) {
    Row {
        Button(onClick = {
            setAttemptedSubmit()
            if (validationErrors.isEmpty()) {
                setWaitingForAuthResp(true)
                scope.launch {
                    try {
                        val authRes = auth.createUserWithEmailAndPassword(
                            email = userEmail,
                            password = userPassword
                        )
                        when (authRes.user != null) {
                            true -> {
                                Logger.i { "User signed in successfully." }
                                onAuthFinished()
                            }

                            false -> {
                                Logger.w { "Sign up failed. User might already exist." }
                                toastError(Exception("User already exists"))
                                setWaitingForAuthResp(false)
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(e) { "Sign up error." }
                        toastError(e)
                    }
                    setWaitingForAuthResp(false)
                }
            } else {
                Logger.i { "Sign up blocked by validation errors: $validationErrors" }
                setAttemptedSubmit()
            }
        }, enabled = !waitingForAuthResp) { Text("Sign Up") }
        Spacer(Modifier.size(16.dp))
        Button(onClick = {
            setAttemptedSubmit()
            if (validationErrors.isEmpty()) {
                setWaitingForAuthResp(true)
                scope.launch {
                    try {
                        val authRes =
                            if (auth.currentUser != null && auth.currentUser!!.isAnonymous) auth.currentUser!!.linkWithCredential(
                                EmailAuthProvider.credential(
                                    email = userEmail,
                                    password = userPassword
                                )
                            ) else
                                auth.signInWithEmailAndPassword(
                                    email = userEmail,
                                    password = userPassword
                                )
                        Logger.i { authRes.toString() }
                        when (auth.currentUser != null && !auth.currentUser!!.isAnonymous) {
                            true -> {
                                Logger.i { "User logged in successfully." }
                                onAuthFinished()
                            }

                            false -> {
                                Logger.e { "Log in failed. Current user is: ${auth.currentUser?.uid}" }
                                toastError(Exception("Log in failed"))
                                setWaitingForAuthResp(false)
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(e) { "Log in error." }
                        toastError(e)
                    }
                    setWaitingForAuthResp(false)
                }
            } else {
                Logger.i { "Log in blocked by validation errors: $validationErrors" }
                setAttemptedSubmit()
            }
        }, enabled = !waitingForAuthResp) { Text("Log In") }
    }

    Spacer(Modifier.size(16.dp))
    HorizontalDivider()
    Spacer(Modifier.size(16.dp))

    TextButton(onClick = openGuestModal, enabled = !waitingForAuthResp) {
        Text("Guest Mode")
    }
}

@Composable
private fun guestModel(
    guestModeDialogOpen: Boolean,
    closeModal: () -> Unit,
    scope: CoroutineScope,
    auth: FirebaseAuth,
    setWaitingForAuthResp: (Boolean) -> Unit = {},
    onAuthFinished: () -> Unit
) {
    if (!guestModeDialogOpen) return

    AlertDialog(
        title = { Text("Continue as guest") },
        text = {
            Text(
                """
                Are you sure you want to enter guest mode? Account information is used
                to sync your data across devices. You can always go to settings to add
                an account later.
                """.trimIndent()
            )
        },
        onDismissRequest = { closeModal() },
        dismissButton = {
            TextButton(
                onClick = { closeModal() })
            { Text("Cancel") }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    setWaitingForAuthResp(true)
                    closeModal()
                    scope.launch {
                        auth.signInAnonymously()
                        setWaitingForAuthResp(false)
                        onAuthFinished()
                    }
                })
            { Text("Confirm") }
        }
    )
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    var lastChar by remember { mutableStateOf<Char?>(null) }
    var showLastChar by remember { mutableStateOf(false) }

    // Show last character briefly when typed
    LaunchedEffect(password) {
        if (password.isNotEmpty() && password.last() != lastChar) {
            lastChar = password.last()
            showLastChar = true
            delay(1000)
            showLastChar = false
        }
    }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = if (showLastChar && password.isNotEmpty()) {
            VisualTransformation { text ->
                TransformedText(
                    buildAnnotatedString {
                        append("•".repeat(password.length - 1))
                        append(password.last())
                    },
                    OffsetMapping.Identity
                )
            }
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        )
    )
}
