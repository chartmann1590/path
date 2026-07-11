package com.biblereadingpath.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblereadingpath.app.data.studyplan.PlanDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlansScreen(
    viewModel: StudyPlansViewModel,
    onNavigateBack: () -> Unit
) {
    val topicPlans = viewModel.topicPlans
    val devotionalPlans = viewModel.devotionalPlans
    val activePlanName by viewModel.activePlanName.collectAsState()
    val activePlanType by viewModel.activePlanType.collectAsState()
    val activePlanId by viewModel.activePlanId.collectAsState()
    val progressMap by viewModel.progressMap.collectAsState()
    var selectedPlan by remember { mutableStateOf<PlanDefinition?>(null) }

    val activePlanKey = "${activePlanType}:${activePlanId ?: ""}"

    if (selectedPlan != null) {
        val planKey = "${selectedPlan!!.typeKey}:${selectedPlan!!.id}"
        PlanDetailDialog(
            plan = selectedPlan!!,
            isActive = planKey == activePlanKey,
            progressMap = progressMap,
            onConfirm = {
                viewModel.selectPlan(selectedPlan!!)
                selectedPlan = null
            },
            onDismiss = { selectedPlan = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Plans") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Current Plan: $activePlanName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Text(
                    text = "Sequential",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            item {
                SequentialPlanCard(
                    isActive = activePlanType == "sequential",
                    onSelect = { viewModel.selectSequential() }
                )
            }

            item {
                Text(
                    text = "Topics",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            items(
                items = topicPlans,
                key = { it.id }
            ) { plan ->
                PlanCard(
                    plan = plan,
                    isActive = "topic:${plan.id}" == activePlanKey,
                    completedCount = plan.chapters.count { ch -> "${ch.book}-${ch.chapter}" in progressMap },
                    onClick = { selectedPlan = plan }
                )
            }

            item {
                Text(
                    text = "Devotionals",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            items(
                items = devotionalPlans,
                key = { it.id }
            ) { plan ->
                PlanCard(
                    plan = plan,
                    isActive = "devotional:${plan.id}" == activePlanKey,
                    completedCount = plan.chapters.count { ch -> "${ch.book}-${ch.chapter}" in progressMap },
                    onClick = { selectedPlan = plan }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SequentialPlanCard(
    isActive: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "\uD83D\uDCD6", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sequential",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Read the Bible from Genesis to Revelation",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isActive) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: PlanDefinition,
    isActive: Boolean,
    completedCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = plan.icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$completedCount/${plan.chapterCount} chapters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (plan.chapterCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = completedCount.toFloat() / plan.chapterCount.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                        )
                    }
                }
            }
            if (isActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PlanDetailDialog(
    plan: PlanDefinition,
    isActive: Boolean,
    progressMap: Set<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${plan.icon} ${plan.name}") },
        text = {
            Column {
                Text(plan.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Chapters included:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                plan.chapters.forEach { ch ->
                    val done = "${ch.book}-${ch.chapter}" in progressMap
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (done) "Completed" else "Not completed",
                            modifier = Modifier.size(16.dp),
                            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${ch.book} ${ch.chapter} - ${ch.label}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isActive) {
                Button(onClick = onConfirm) {
                    Text("Switch to This Plan")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Currently Active")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
