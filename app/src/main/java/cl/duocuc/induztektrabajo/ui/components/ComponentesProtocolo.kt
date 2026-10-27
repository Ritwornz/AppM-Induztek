package cl.duocuc.induztektrabajo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duocuc.induztektrabajo.model.EstadoProtocolo
import cl.duocuc.induztektrabajo.model.ProtocoloResumen
import cl.duocuc.induztektrabajo.ui.theme.AmarilloEstado
import cl.duocuc.induztektrabajo.ui.theme.NaranjaInduztek
import cl.duocuc.induztektrabajo.ui.theme.RojoEstado
import cl.duocuc.induztektrabajo.ui.theme.VerdeEstado

private fun colorEstado(estado: EstadoProtocolo): Color = when (estado) {
    EstadoProtocolo.BORRADOR -> NaranjaInduztek
    EstadoProtocolo.REGISTRADO_TERRENO -> Color(0xFF1E6FB8)
    EstadoProtocolo.OBSERVADO -> AmarilloEstado
    EstadoProtocolo.FORMALIZADO -> VerdeEstado
    EstadoProtocolo.ANULADO -> RojoEstado
}

@Composable
fun BotonVolverFlecha(onClick: () -> Unit, descripcion: String = "Volver") {
    IconButton(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = descripcion }
    ) {
        Text("←", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChipEstado(estado: EstadoProtocolo) {
    val color = colorEstado(estado)
    Text(
        text = estado.etiqueta,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun BuscadorTag(texto: String, onCambia: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = texto,
        onValueChange = onCambia,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Buscar por TAG, cliente o N° protocolo") },
        placeholder = { Text("Ej: TR-01") }
    )
}

@Composable
fun TarjetaMetrica(titulo: String, valor: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                valor.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TarjetaProtocolo(protocolo: ProtocoloResumen, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    protocolo.equipo.tag,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                ChipEstado(protocolo.estado)
            }
            Spacer(Modifier.height(4.dp))
            Text(protocolo.equipo.tipo.etiqueta, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${protocolo.equipo.cliente.nombre} · ${protocolo.equipo.cliente.instalacion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    protocolo.numero,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${protocolo.fecha} · Pruebas ${protocolo.pruebasAprobadas}/${protocolo.pruebasTotales}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
