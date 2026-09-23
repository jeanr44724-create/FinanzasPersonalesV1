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

// ============================================================
// MODELOS
// ============================================================

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

// ============================================================
// VIEWMODEL
// ============================================================

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

    val income
        get() = movements
            .filter { it.type == "income" }
            .sumOf { it.amount }

    val expenses
        get() = movements
            .filter { it.type == "expense" }
            .sumOf { it.amount }

    val savings
        get() = goals.sumOf { it.saved }

    val balance
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

// ============================================================
// FUNCIONES
// ============================================================

fun money(value: Double): String {
    return "$" + "%.2f".format(value)
}

// ============================================================
// COLORES DE LA APLICACIÓN
// ============================================================

val FinanceBlue = Color(0xFF102A43)
val FinanceBlueLight = Color(0xFF1F4E79)

val FinanceGreen = Color(0xFF16A085)
val FinanceGreenLight = Color(0xFFE8F8F5)

val FinanceRed = Color(0xFFE74C3C)
val FinanceRedLight = Color(0xFFFDEDEC)

val FinanceOrange = Color(0xFFF39C12)
val FinanceOrangeLight = Color(0xFFFEF5E7)

val FinanceBackground = Color(0xFFF5F7FA)
val FinanceText = Color(0xFF17202A)
val FinanceGray = Color(0xFF6B7280)

// ============================================================
// TEMA
// ============================================================

@Composable
fun MainTheme(content: @Composable () -> Unit) {

    val colors = lightColorScheme(

        primary = FinanceBlue,
        onPrimary = Color.White,

        secondary = FinanceGreen,
        onSecondary = Color.White,

        background = FinanceBackground,
        onBackground = FinanceText,

        surface = Color.White,
        onSurface = FinanceText,

        error = FinanceRed,
        onError = Color.White
    )

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}

// ============================================================
// APLICACIÓN
// ============================================================

@Composable
fun App(vm: FinanceViewModel) {

    val navController = rememberNavController()

    Scaffold(

        containerColor = FinanceBackground,

        bottomBar = {

            NavigationBar(
                containerColor = Color.White
            ) {

                val items = listOf(

                    Triple(
                        "inicio",
                        "Inicio",
                        Icons.Default.Home
                    ),

                    Triple(
                        "movimientos",
                        "Movimientos",
                        Icons.Default.SwapHoriz
                    ),

                    Triple(
                        "ahorros",
                        "Ahorros",
                        Icons.Default.Savings
                    ),

                    Triple(
                        "estadisticas",
                        "Stats",
                        Icons.Default.BarChart
                    ),

                    Triple(
                        "deudas",
                        "Deudas",
                        Icons.Default.CreditCard
                    ),

                    Triple(
                        "salario",
                        "Salario",
                        Icons.Default.Payments
                    )
                )

                items.forEach { item ->

                    NavigationBarItem(

                        selected =
                            navController.currentDestination?.route ==
                                    item.first,

                        onClick = {

                            navController.navigate(item.first) {

                                launchSingleTop = true

                            }
                        },

                        icon = {

                            Icon(
                                imageVector = item.third,
                                contentDescription = item.second
                            )
                        },

                        label = {
                            Text(item.second)
                        }
                    )
                }
            }
        }

    ) { padding ->

        NavHost(

            navController = navController,

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

// ============================================================
// INICIO
// ============================================================

@Composable
fun HomeScreen(vm: FinanceViewModel) {

    var showMovementDialog by remember {
        mutableStateOf(false)
    }

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(horizontal = 18.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp),

        contentPadding = PaddingValues(
            top = 20.dp,
            bottom = 30.dp
        )
    ) {

        item {

            Text(
                text = "Mis Finanzas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = FinanceText
            )

            Text(
                text = "Controla tu dinero de forma sencilla",
                color = FinanceGray,
                fontSize = 14.sp
            )
        }

        item {

            BalanceCard(
                balance = vm.balance
            )
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                FinanceSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Ingresos",
                    value = money(vm.income),
                    icon = Icons.Default.TrendingUp,
                    iconColor = FinanceGreen
                )

                FinanceSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Gastos",
                    value = money(vm.expenses),
                    icon = Icons.Default.TrendingDown,
                    iconColor = FinanceRed
                )
            }
        }

        item {

            SavingsOverview(vm)
        }

        item {

            Text(
                text = "Acciones rápidas",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        item {

            Button(

                onClick = {
                    showMovementDialog = true
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = FinanceBlue
                )
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

        item {

            OutlinedButton(

                onClick = { },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                shape = RoundedCornerShape(16.dp)
            ) {

                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text("Ver estadísticas")
            }
        }
    }

    if (showMovementDialog) {

        MovementDialog(vm) {

            showMovementDialog = false
        }
    }
}

// ============================================================
// TARJETA DE SALDO
// ============================================================

@Composable
fun BalanceCard(balance: Double) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = FinanceBlue
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
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
                        color = Color.White.copy(alpha = 0.8f)
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

            Spacer(Modifier.height(15.dp))

            Text(
                "Este es el dinero restante según tus movimientos registrados.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
        }
    }
}

// ============================================================
// TARJETAS RESUMEN
// ============================================================

@Composable
fun FinanceSummaryCard(

    modifier: Modifier = Modifier,

    title: String,

    value: String,

    icon: androidx.compose.ui.graphics.vector.ImageVector,

    iconColor: Color

) {

    Card(

        modifier = modifier,

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                title,
                color = FinanceGray,
                fontSize = 13.sp
            )

            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        }
    }
}

// ============================================================
// RESUMEN DE AHORROS
// ============================================================

@Composable
fun SavingsOverview(vm: FinanceViewModel) {

    val goal = vm.goals.firstOrNull()

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
                        font
