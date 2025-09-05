package com.example.underbigtreeapplication.ui.pointPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.underbigtreeapplication.viewModel.RewardViewModel

@Composable
fun RewardsScreen(
    onBackClick: () -> Unit = {},
    onRedeemClick: () -> Unit = {},
    viewModel: RewardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val rewardsList by viewModel.rewards.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text("Rewards", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Points: 100",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )

        var selectedRewardId by remember { mutableStateOf<String?>(null) }

        LazyColumn {
            items(rewardsList) { reward ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(reward.name, fontWeight = FontWeight.Bold)
                            Text("${reward.pointsRequired} pts")
                            Text(reward.condition, fontSize = 12.sp, color = Color.Gray)
                        }
                        RadioButton(
                            selected = selectedRewardId == reward.id,
                            onClick = {
                                selectedRewardId = reward.id
                                viewModel.selectReward(reward)
                            }
                        )
                    }
                }
            }
        }

        var errorMessage by remember { mutableStateOf<String?>(null) }
        Button(
            onClick = {
                val reward = viewModel.selectedReward.value
//                if (reward != null) {
//                    if (currentPoints >= reward.pointsRequired) {
//                        viewModel.redeemSelectedReward()
//                        currentPoints -= reward.pointsRequired  // ✅ update the state
//                        errorMessage = null
//                        onRedeemClick()
//                    } else {
//                        errorMessage = "Points is not enough to redeem"
//                    }
//                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = selectedRewardId != null
        ) {
            Text("Redeem")
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
