package com.example.finanzaspersonales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import java.util.UUID

// ==================== MODELOS ====================

data class Movement(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val amount: Double,
    val category: String,
    val note: String = ""
)

data class SavingGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val target: Double,
    val saved: Double = 0.0
)

// ==================== VIEWMODEL ====================

class FinanceViewModel : ViewModel() {

    var movements by mutableStateOf(listOf<Movement>())
        private set

    var goals by mutableStateOf(
        listOf(
            SavingGoal(
                name = "Meta de ahorro",
                target = 7000.0
            )
        )
    )
        private set

    val income: Double
        get() = movements
            .filter { it.type == "income" }
            .sumOf { it.amount }

    val expenses: Double
        get() = movements
            .filter { it.type == "expense" }
            .sumOf { it.amount }

    val savings: Double
        get() = goals.sumOf { it.saved }

    val balance: Double
        get() = income - expenses

    fun addMovement(
        type: String,
        amount: Double,
        category: String,
        note: String
    ) {
        if (amount > 0) {
            movements = movements + Movement(
                type = type,
                amount = amount,
                category = category,
                note = note
            )
        }
    }

    fun deleteMovement(id: String) {
        movements = movements.filterNot { it.id == id }
    }

    fun addGoal(name: String, target: Double) {
        if (name.isNotBlank() && target > 0) {
            goals = goals + SavingGoal(
                name = name,
                target = target
            )
        }
    }

    fun addToGoal(id: String, amount: Double) {
        if (amount <= 0) return

        goals = goals.map {
            if (it.id == id) {
                it.copy(
                    saved = (it.saved + amount)
                        .coerceAtMost(it.target)
                )
            } else {
                it
            }
        }
    }
}

fun money(value: Double): String =
    "$" + "%.2f".format(value)

// ==================== COLORES ====================

val FinanceBlue = Color(0xFF12355B)
val FinanceBlue2 = Color(0xFF1E6091)
val FinanceGreen = Color(0xFF16A085)
val FinanceRed = Color(0xFFE74C3C)
val FinanceOrange = Color(0xFFF39C12)
val FinanceBackground = Color(0xFFF4F7FA)
val FinanceGray = Color(0xFF6B7280)

// ==================== TEMA ====================

@Composable
fun MainTheme(content: @Composable () -> Unit) {

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = FinanceBlue,
            secondary = FinanceGreen,
            background = FinanceBackground,
            surface = Color.White,
            error = FinanceRed
        ),
        content = content
    )
}

// ==================== APP ====================

@Composable
fun App(vm: FinanceViewModel) {

    val nav = rememberNavController()

    Scaffold(
        containerColor = FinanceBackground,

        bottomBar = {

            NavigationBar(
                containerColor = Color.White
            ) {

                val tabs = listOf(
                    Triple("inicio", "Inicio", Icons.Default.Home),
                    Triple("movimientos", "Movimientos", Icons.Default.SwapHoriz),
                    Triple("ahorros", "Ahorros", Icons.Default.Savings),
                    Triple("estadisticas", "Stats", Icons.Default.BarChart),
                    Triple("deudas", "Deudas", Icons.Default.CreditCard),
                    Triple("salario", "Salario", Icons.Default.Payments)
                )

                tabs.forEach { tab ->

                    NavigationBarItem(
                        selected = nav.currentDestination?.route == tab.first,
                        onClick = {
                            nav.navigate(tab.first) {
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(tab.third, contentDescription = tab.second)
                        },
                        label = {
                            Text(tab.second)
                        }
                    )
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = nav,
            startDestination = "inicio",
            modifier = Modifier.padding(padding)
        ) {

            composable("inicio") {
                HomeScreen(vm)
            }

            composable("movimientos") {
                MovementsScreen(vm)
            }

            composable("ahorros") {
                SavingsScreen(vm)
            }

            composable("estadisticas") {
                StatsScreen(vm)
            }

            composable("deudas") {
                DebtScreen()
            }

            composable("salario") {
                SalaryScreen()
            }
        }
    }
}

// ==================== INICIO ====================

@Composable
fun HomeScreen(vm: FinanceViewModel) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(horizontal = 18.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp),

        contentPadding = PaddingValues(
            top = 20.dp,
            bottom = 25.dp
        )
    ) {

        item {

            Text(
                "Mis Finanzas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Controla tu dinero de forma sencilla",
                color = FinanceGray
            )
        }

        item {
            BalanceCard(vm.balance)
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SummaryCard(
                    Modifier.weight(1f),
                    "Ingresos",
                    money(vm.income),
                    Icons.Default.TrendingUp,
                    FinanceGreen
                )

                SummaryCard(
                    Modifier.weight(1f),
                    "Gastos",
                    money(vm.expenses),
                    Icons.Default.TrendingDown,
                    FinanceRed
                )
            }
        }

        item {
            SavingsCard(vm)
        }

        item {

            Text(
                "Acciones rápidas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {

            Button(
                onClick = {
                    showDialog = true
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(16.dp)
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Registrar movimiento",
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showDialog) {
        MovementDialog(vm) {
            showDialog = false
        }
    }
}

// ==================== SALDO ====================

@Composable
fun BalanceCard(balance: Double) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = FinanceBlue
        )
    ) {

        Column(
            modifier = Modifier.padding(22.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        "Saldo disponible",
                        color = Color.White.copy(alpha = .75f)
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        money(balance),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Ingresos menos gastos registrados",
                color = Color.White.copy(alpha = .7f),
                fontSize = 12.sp
            )
        }
    }
}

// ==================== RESUMEN ====================

@Composable
fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier = Modifier.padding(15.dp)
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                title,
                color = FinanceGray,
                fontSize = 13.sp
            )

            Text(
                value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== AHORROS EN INICIO ====================

@Composable
fun SavingsCard(vm: FinanceViewModel) {

    val goal = vm.goals.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        "Meta de ahorro",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (goal != null) {

                        Text(
                            "${money(goal.saved)} de ${money(goal.target)}",
                            color = FinanceGray,
                            fontSize = 13.sp
                        )
                    }
                }

                Icon(
                    Icons.Default.Savings,
                    contentDescription = null,
                    tint = FinanceGreen
                )
            }

            if (goal != null) {

                Spacer(Modifier.height(12.dp))

                val progress =
                    (goal.saved / goal.target)
                        .toFloat()
                        .coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = FinanceGreen
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "${(progress * 100).toInt()}% completado",
                    color = FinanceGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==================== MOVIMIENTOS ====================

@Composable
fun MovementsScreen(vm: FinanceViewModel) {

    var show by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {

                Text(
                    "Movimientos",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Tus ingresos y gastos",
                    color = FinanceGray
                )
            }

            FloatingActionButton(
                onClick = {
                    show = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )
            }
        }

       
