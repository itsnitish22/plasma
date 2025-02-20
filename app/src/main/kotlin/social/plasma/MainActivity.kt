package social.plasma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.slack.circuit.CircuitCompositionLocals
import com.slack.circuit.CircuitConfig
import com.slack.circuit.NavigableCircuitContent
import com.slack.circuit.Screen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.push
import com.slack.circuit.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import social.plasma.domain.interactors.SyncContactsEvents
import social.plasma.domain.interactors.SyncMyEvents
import social.plasma.features.onboarding.screens.HeadlessAuthenticator
import social.plasma.ui.theme.PlasmaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var circuitConfig: CircuitConfig

    @Inject
    lateinit var syncMyEvents: SyncMyEvents

    @Inject
    lateinit var syncMyContactsEvents: SyncContactsEvents

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val startScreens: List<Screen> = listOf(HeadlessAuthenticator)

        setContent {
            // TODO find a better home for these
            LaunchedEffect(Unit) { syncMyEvents.executeSync(Unit) }
            LaunchedEffect(Unit) {
                // TODO this API looks awkward. But perhaps it'll make more sense outside of compose
                syncMyContactsEvents.apply {
                    invoke(Unit)
                    flow.collect {}
                }
            }

            PlasmaTheme(dynamicStatusBar = true) {
                val backstack =
                    rememberSaveableBackStack { startScreens.forEach { screen -> push(screen) } }
                val circuitNavigator = rememberCircuitNavigator(backstack)
                BackHandler(enabled = backstack.size > 1, onBack = circuitNavigator::pop)
                Surface(color = MaterialTheme.colorScheme.background) {
                    CircuitCompositionLocals(circuitConfig) {
                        ContentWithOverlays {
                            NavigableCircuitContent(circuitNavigator, backstack)
                        }
                    }
                }
            }
        }
    }
}
