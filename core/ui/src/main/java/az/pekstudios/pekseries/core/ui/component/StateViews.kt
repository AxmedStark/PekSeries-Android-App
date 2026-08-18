package az.pekstudios.pekseries.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.ui.theme.PekYellow
import az.pekstudios.pekseries.core.ui.theme.Primary

/**
 * User-facing copy for a failure.
 *
 * Kept in one place so every screen describes the same problem the same way,
 * and so the wording can be localised later without hunting through features.
 */
fun DataError.toUserMessage(): String = when (this) {
    DataError.Network -> "No internet connection. Check your network and try again."
    DataError.RateLimited -> "Too many requests right now. Give it a moment and retry."
    DataError.NotFound -> "We couldn't find that show."
    DataError.Unauthenticated -> "Please sign in again to continue."
    DataError.Server -> "The service is having trouble. Try again shortly."
    is DataError.Unknown -> "Something went wrong. Please try again."
}

@Composable
fun PekLoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
fun PekErrorView(
    error: DataError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = error.toUserMessage(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (onRetry != null) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text("Retry", color = PekYellow)
            }
        }
    }
}

@Composable
fun PekEmptyView(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
