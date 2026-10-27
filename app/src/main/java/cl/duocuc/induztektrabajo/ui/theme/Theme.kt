package cl.duocuc.induztektrabajo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = NaranjaInduztek,
    onPrimary = SuperficieClara,
    primaryContainer = NaranjaSuave,
    onPrimaryContainer = NaranjaOscuro,
    background = FondoClaro,
    onBackground = TextoClaroPrincipal,
    surface = SuperficieClara,
    onSurface = TextoClaroPrincipal,
    onSurfaceVariant = TextoClaroSecundario,
    outline = BordeClaro,
    error = RojoEstado
)

private val EsquemaOscuro = darkColorScheme(
    primary = NaranjaInduztek,
    onPrimary = FondoOscuro,
    primaryContainer = NaranjaOscuro,
    onPrimaryContainer = SuperficieClara,
    background = FondoOscuro,
    onBackground = TextoOscuroPrincipal,
    surface = SuperficieOscura,
    onSurface = TextoOscuroPrincipal,
    onSurfaceVariant = TextoOscuroSecundario,
    outline = BordeOscuro,
    error = RojoEstado
)

private val FormasInduztek = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun InduztekTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = TipografiaInduztek,
        shapes = FormasInduztek,
        content = content
    )
}

@Composable
fun MiRegistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = InduztekTheme(darkTheme = darkTheme, content = content)
