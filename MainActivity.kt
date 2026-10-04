package com.postal.tabillmanager

import os
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Route(val id: Int, val fromOffice: String, val toOffice: String, val distanceKm: Double, val timeMins: Int)
data class TABillEntry(val date: String, val fromOffice: String, val toOffice: String, val distanceKm: Double, val ratePerKm: Double, val claimAmount: Double, val workDetails: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle()) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TABillAppMain()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TABillAppMain() {
    var selectedTab by remember { mutableStateOf(0) }
    
    val masterRoutes = remember {
        mutableStateListOf(
            Route(1, "DO, JORHAT", "BADULIPAR SO", 43.0, 90),
            Route(2, "DO, JORHAT", "BORHOLLA SO", 40.0, 80),
            Route(3, "DO, JORHAT", "BAHONA SO", 10.0, 25),
            Route(4, "DO, JORHAT", "CINNAMARA SO", 6.0, 20),
            Route(5, "DO, JORHAT", "MARIANI MDG", 20.0, 40),
            Route(6, "DO, JORHAT", "TEOK SO", 25.0, 50),
            Route(7, "DO, JORHAT", "TITABAR SO", 20.0, 40)
        )
    }

    val taEntries = remember {
        mutableStateListOf(
            TABillEntry("05/10/2026", "DO, JORHAT", "TITABAR SO", 20.0, 9.0, 180.0, "Resolved ECMP GPS sync"),
            TABillEntry("08/10/2026", "DO, JORHAT", "TEOK SO", 25.0, 9.0, 225.0, "Restored postal network router")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Postal TA & Diary Generator", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Daily Entry") },
                    icon = { Text("📝") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("TA Claims") },
                    icon = { Text("📄") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Master Routes") },
                    icon = { Text("🗺️") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> DailyEntryScreen(masterRoutes) { entry -> taEntries.add(entry) }
                1 -> TAClaimsScreen(taEntries)
                2 -> MasterRoutesScreen(masterRoutes) { route -> masterRoutes.add(route) }
            }
        }
    }
}

@Composable
fun DailyEntryScreen(routes: List<Route>, onSaveEntry: (TABillEntry) -> Unit) {
    var date by remember { mutableStateOf("05/10/2026") }
    var selectedFrom by remember { mutableStateOf("DO, JORHAT") }
    var selectedTo by remember { mutableStateOf("TITABAR SO") }
    var rateKm by remember { mutableStateOf("9.0") }
    var workDetails by remember { mutableStateOf("") }
    
    val matchedDistance = routes.find { it.fromOffice == selectedFrom && it.toOffice == selectedTo }?.distanceKm ?: 0.0
    val calculatedClaim = matchedDistance * (rateKm.toDoubleOrNull() ?: 0.0)

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Log New Inspection / Tour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = selectedFrom, onValueChange = { selectedFrom = it }, label = { Text("From Office") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = selectedTo, onValueChange = { selectedTo = it }, label = { Text("To Office") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Auto Distance: $matchedDistance KM", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = rateKm, onValueChange = { rateKm = it }, label = { Text("Rate per KM (₹)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = workDetails, onValueChange = { workDetails = it }, label = { Text("Work Executed / Ref Ticket") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Calculated TA Claim Amount:", fontWeight = FontWeight.Bold)
                Text("₹ %.2f".format(calculatedClaim), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSaveEntry(TABillEntry(date, selectedFrom, selectedTo, matchedDistance, rateKm.toDoubleOrNull() ?: 0.0, calculatedClaim, workDetails))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Entry to TA Bill & Diary")
        }
    }
}

@Composable
fun TAClaimsScreen(entries: List<TABillEntry>) {
    val totalClaim = entries.sumOf { it.claimAmount }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Monthly TA Claims Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Total Payable: ₹ %.2f".format(totalClaim), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(entries) { entry ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${entry.date}: ${entry.fromOffice} ➔ ${entry.toOffice}", fontWeight = FontWeight.Bold)
                        Text("Distance: ${entry.distanceKm} KM @ ₹${entry.ratePerKm}/km = ₹${entry.claimAmount}")
                        Text("Work: ${entry.workDetails}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MasterRoutesScreen(routes: List<Route>, onAddRoute: (Route) -> Unit) {
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var dist by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Manage Master Office Distance Data", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = from, onValueChange = { from = it }, label = { Text("From") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = to, onValueChange = { to = it }, label = { Text("To") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = dist, onValueChange = { dist = it }, label = { Text("KM") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Mins") }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (from.isNotEmpty() && to.isNotEmpty()) {
                    onAddRoute(Route(routes.size + 1, from, to, dist.toDoubleOrNull() ?: 0.0, time.toIntOrNull() ?: 0))
                    from = ""; to = ""; dist = ""; time = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Route to Master Data")
        }

        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            items(routes) { route ->
                ListItem(
                    headlineContent = { Text("${route.fromOffice} ➔ ${route.toOffice}") },
                    supportingContent = { Text("Distance: ${route.distanceKm} KM | Time: ${route.timeMins} Mins") }
                )
                Divider()
            }
        }
    }
}
