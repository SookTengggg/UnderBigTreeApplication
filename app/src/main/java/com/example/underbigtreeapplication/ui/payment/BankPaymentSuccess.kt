package com.example.underbigtreeapplication.ui.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.utils.formatAmount
import com.example.underbigtreeapplication.viewModel.OrderSummaryViewModel

@Composable
fun BankPaymentSuccess(
    totalAmount: Double,
    viewModel: OrderSummaryViewModel = viewModel(),
    onDoneClick: () -> Unit = {}
) {
    val transactionDate = viewModel.getTransactionDate()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Image(
            painter = painterResource(id = R.drawable.bank_success),
            contentDescription = "Bank Success",
            modifier = Modifier
                .size(75.dp)
                .padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Transaction Successful!",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Please return to your desktop to proceed.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = Color.Gray
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Spacer(modifier = Modifier.height(60.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RowItem("Transaction Type", "Pay Now Transfer")
            RowItem("Transfer To", "UNDER BIG TREE")
            RowItem("Amount", "${formatAmount(totalAmount)}")
            RowItem("Transaction Date", transactionDate)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onDoneClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF9D648),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Done")
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun RowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}