package com.example.weathermap.presentation.map.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.weathermap.R
import com.example.weathermap.domain.model.TemperatureRange
import com.example.weathermap.domain.model.WeatherCondition
import com.example.weathermap.domain.model.WeatherFilter

@Composable
fun FilterBar(
    filter: WeatherFilter,
    matchingCount: Int,
    onSearchQueryChanged: (String) -> Unit,
    onConditionSelected: (WeatherCondition?) -> Unit,
    onTemperatureRangeSelected: (TemperatureRange) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = filter.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (filter.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(id = R.string.search_clear)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Condition Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Condition: All
                FilterChip(
                    selected = filter.selectedCondition == null,
                    onClick = { onConditionSelected(null) },
                    label = { Text(text = stringResource(id = R.string.filter_condition_all)) }
                )

                // Condition: Clear
                FilterChip(
                    selected = filter.selectedCondition == WeatherCondition.CLEAR,
                    onClick = { onConditionSelected(WeatherCondition.CLEAR) },
                    label = { Text(text = stringResource(id = R.string.filter_condition_clear)) }
                )

                // Condition: Cloudy
                FilterChip(
                    selected = filter.selectedCondition == WeatherCondition.CLOUDY,
                    onClick = { onConditionSelected(WeatherCondition.CLOUDY) },
                    label = { Text(text = stringResource(id = R.string.filter_condition_cloudy)) }
                )

                // Condition: Rain
                FilterChip(
                    selected = filter.selectedCondition == WeatherCondition.RAIN,
                    onClick = { onConditionSelected(WeatherCondition.RAIN) },
                    label = { Text(text = stringResource(id = R.string.filter_condition_rain)) }
                )

                // Condition: Snow
                FilterChip(
                    selected = filter.selectedCondition == WeatherCondition.SNOW,
                    onClick = { onConditionSelected(WeatherCondition.SNOW) },
                    label = { Text(text = stringResource(id = R.string.filter_condition_snow)) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Temperature Filter Chips & Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Temp: All
                FilterChip(
                    selected = filter.temperatureRange == TemperatureRange.ALL,
                    onClick = { onTemperatureRangeSelected(TemperatureRange.ALL) },
                    label = { Text(text = stringResource(id = R.string.filter_temp_all)) }
                )

                // Temp: >15°C
                FilterChip(
                    selected = filter.temperatureRange == TemperatureRange.WARM,
                    onClick = { onTemperatureRangeSelected(TemperatureRange.WARM) },
                    label = { Text(text = stringResource(id = R.string.filter_temp_warm)) }
                )

                // Temp: 0°C to 15°C
                FilterChip(
                    selected = filter.temperatureRange == TemperatureRange.MILD,
                    onClick = { onTemperatureRangeSelected(TemperatureRange.MILD) },
                    label = { Text(text = stringResource(id = R.string.filter_temp_mild)) }
                )

                // Temp: <0°C
                FilterChip(
                    selected = filter.temperatureRange == TemperatureRange.COLD,
                    onClick = { onTemperatureRangeSelected(TemperatureRange.COLD) },
                    label = { Text(text = stringResource(id = R.string.filter_temp_cold)) }
                )
            }

            // Status bar showing matching station count and reset button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.filtered_results_count, matchingCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (filter.isActive) {
                    TextButton(onClick = onResetFilters) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(id = R.string.filter_reset))
                    }
                }
            }
        }
    }
}
