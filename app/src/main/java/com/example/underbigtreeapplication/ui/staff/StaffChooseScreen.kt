package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.underbigtreeapplication.viewModel.StaffViewModel

@Composable
fun StaffChooseScreen(
    navController: NavController,
    staffViewModel: StaffViewModel,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("Food", "Drink", "Sauce", "Add On")

    Dialog(onDismissRequest = { onDismiss() }) {

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add for...", fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))

                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = { onOptionSelected(option) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("CANCEL")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedOption?.let { option ->
                                when (option) {
                                    "Food" -> navController.navigate("addFood")
                                    "Drink" -> navController.navigate("addDrink")
                                    "Sauce" -> navController.navigate("addSauce")
                                    "Add On" -> navController.navigate("addAddOn")
                                }
                                onDismiss()
                            }
                        },
                        enabled = selectedOption != null
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}