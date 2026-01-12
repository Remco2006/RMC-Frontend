package com.example.rmcfrontend.ui.theme.screens.cars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.R

@Composable
fun CarItem(
    car: Car,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {

            AsyncImage(
                model = car.imageFileNames.firstOrNull()?.let { "http://10.0.2.2:8080/images/$it" },
                placeholder = painterResource(R.drawable.car),
                error = painterResource(R.drawable.car),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 88.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "${car.make.orEmpty()} ${car.model.orEmpty()}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Year: ${car.modelYear ?: "-"} • ${car.color ?: "-"}",
                    fontSize = 13.sp
                )
                Text(
                    text = car.price?.let { "€ %.2f".format(it) } ?: "",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
