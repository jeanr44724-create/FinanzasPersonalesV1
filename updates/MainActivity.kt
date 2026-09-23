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

data class Movement(
    val id: String = System.currentTimeMillis().toString(),
    val type: String,
    val amount: Double,
    val category: String,
    val note: String = ""
)

data class SavingGoal(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val target: Double,
    val saved: Double = 0.0
)

class FinanceViewModel : ViewModel() {

    var movements by mutableStateOf<List<Movement>>(emptyList())
        private set

    var goals by mutableStateOf(
        listOf(
            SavingGoal(
                name = "Meta principal",
                target = 7000.0,
                saved = 0.0
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

    val balance: Double
        get() = income - expenses

    fun addMovement(
        type: String,
        amount: Double,
        category: String,
        note: String
    ) {
        movements = movements + Movement(
            type = type,
            amount = amount,
            category = category,
            note = note
        )
    }

    fun deleteMovement(id: String) {
        movements = movements.filterNot { it.id == id }
    }

    fun addGoal(name: String, target: Double) {
        goals = goals + SavingGoal(
            name = name,
            target = target
        )
    }

    fun addToGoal(id: String, amount: Double) {
        goals = goals.map {
            if (it.id == id) {
                it.copy(saved = it.saved + amount)
            } else {
                it
            }
        }
    }
}

private val FinanceBlue = Color(0xFF12355B)
private val FinanceBlue2 = Color(0xFF1E6091)
private val FinanceGreen = Color(0xFF16A085)
private val FinanceRed = Color(0xFFE74C3C)
private val FinanceOrange = Color(0xFFF39C12)
private val FinanceBackground = Color(0xFFF4F7FA)
private val FinanceGray = Color(0xFF6B7280)

private fun money(value: Double): String {
    return "$" + String.format("%.2f", value)
}

@Composable
fun MainTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = FinanceBlue,
        secondary = FinanceGreen,
        background = FinanceBackground,
        surface = Color.White
    )

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Composable
fun App(vm: FinanceViewModel) {

    val navController = rememberNavController()

    val items = listOf(
        Triple("inicio", "Inicio", Icons.Default.Home),
        Triple("movimientos", "Movimientos", Icons.Default.List),
        Triple("ahorros", "Ahorros", Icons.Default.Savings),
        Triple("estadisticas", "Stats", Icons.Default.BarChart),
        Triple("deudas", "Deudas", Icons.Default.AccountBalance),
        Triple("salario", "Salario", Icons.Default.Payments)
    )

    Scaffold(
        containerColor = FinanceBackground,
        bottomBar = {
            NavigationBar {
                items.forEach { item ->

                    val selected =
                        navController.currentBackStackEntryAsState()
                            .value?.destination?.route == item.first

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.first) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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

@Composable
fun HomeScreen(vm: FinanceViewModel) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                text = "Mis Finanzas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = FinanceBlue
            )

            Text(
                text = "Controla tu dinero de forma inteligente",
                color = FinanceGray
            )
        }

        item {
            BalanceCard(vm.balance)
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Ingresos",
                    value = money(vm.income),
                    color = FinanceGreen,
                    icon = Icons.Default.TrendingUp
                )

                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Gastos",
                    value = money(vm.expenses),
                    color = FinanceRed,
                    icon = Icons.Default.TrendingDown
                )
            }
        }

        item {
            SavingsCard(vm)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FinanceBlue
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Resumen financiero",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (vm.balance >= 0) {
                            "Tienes ${money(vm.balance)} disponibles después de tus gastos."
                        } else {
                            "Tus gastos superan tus ingresos por ${money(-vm.balance)}."
                        },
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = FinanceBlue
        )
    ) {

        Column(
            modifier = Modifier.padding(24.dp)
        ) {

            Text(
                text = "Balance disponible",
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = money(balance),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Actualizado con tus movimientos",
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = FinanceGray
            )

            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SavingsCard(vm: FinanceViewModel) {

    val goal = vm.goals.firstOrNull()

    if (goal == null) return

    val progress =
        if (goal.target > 0) {
            (goal.saved / goal.target).coerceIn(0.0, 1.0)
        } else {
            0.0
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Meta de ahorro",
                        color = FinanceGray
                    )

                    Text(
                        text = goal.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = FinanceGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${money(goal.saved)} de ${money(goal.target)}",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp),
                color = FinanceGreen,
                trackColor = FinanceBackground
            )
        }
    }
}

@Composable
fun MovementsScreen(vm: FinanceViewModel) {

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FinanceBackground,
        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showDialog = true
                },
                containerColor = FinanceBlue
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar"
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {

                Text(
                    text = "Movimientos",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinanceBlue,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        bottom = 10.dp
                    )
                )
            }

            if (vm.movements.isEmpty()) {

                item {
                    EmptyState(
                        icon = Icons.Default.ReceiptLong,
                        title = "No tienes movimientos",
                        message = "Agrega tu primer ingreso o gasto."
                    )
                }

            } else {

                items(
                    items = vm.movements.reversed(),
                    key = { it.id }
                ) { movement ->

                    MovementCard(
                        movement = movement,
                        onDelete = {
                            vm.deleteMovement(movement.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDialog) {

        MovementDialog(
            onDismiss = {
                showDialog = false
            },
            onSave = { type, amount, category, note ->

                vm.addMovement(
                    type = type,
                    amount = amount,
                    category = category,
                    note = note
                )

                showDialog = false
            }
        )
    }
}

@Composable
fun MovementCard(
    movement: Movement,
    onDelete: () -> Unit
) {

    val isIncome = movement.type == "income"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = if (isIncome) {
                    Icons.Default.TrendingUp
                } else {
                    Icons.Default.TrendingDown
                },
                contentDescription = null,
                tint = if (isIncome) FinanceGreen else FinanceRed
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = movement.category,
                    fontWeight = FontWeight.Bold
                )

                if (movement.note.isNotBlank()) {

                    Text(
                        text = movement.note,
                        color = FinanceGray,
                        fontSize = 13.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = if (isIncome) {
                        "+${money(movement.amount)}"
                    } else {
                        "-${money(movement.amount)}"
                    },
                    color = if (isIncome) FinanceGreen else FinanceRed,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDelete
                ) {

                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = FinanceGray
                    )
                }
            }
        }
    }
}

@Composable
fun MovementDialog(
    onDismiss: () -> Unit,
    onSave: (
        String,
        Double,
        String,
        String
    ) -> Unit
) {

    var type by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Nuevo movimiento",
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(
                        selected = type == "expense",
                        onClick = {
                            type = "expense"
                        },
                        label = {
                            Text("Gasto")
                        }
                    )

                    FilterChip(
                        selected = type == "income",
                        onClick = {
                            type = "income"
                        },
                        label = {
                            Text("Ingreso")
                        }
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                    },
                    label = {
                        Text("Monto")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    label = {
                        Text("Categoría")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    label = {
                        Text("Nota")
                    },
                    singleLine = true
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val value = amount.toDoubleOrNull()

                    if (value != null && value > 0 && category.isNotBlank()) {

                        onSave(
                            type,
                            value,
                            category,
                            note
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SavingsScreen(vm: FinanceViewModel) {

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FinanceBackground,
        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showDialog = true
                },
                containerColor = FinanceGreen
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva meta"
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                Text(
                    text = "Mis ahorros",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinanceBlue
                )

                Text(
                    text = "Construye tus metas paso a paso",
                    color = FinanceGray
                )
            }

            items(vm.goals) { goal ->

                GoalCard(
                    goal = goal,
                    onAdd = {
                        vm.addToGoal(goal.id, 50.0)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDialog) {

        NewGoalDialog(
            onDismiss = {
                showDialog = false
            },
            onSave = { name, target ->

                vm.addGoal(name, target)

                showDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: SavingGoal,
    onAdd: () -> Unit
) {

    val progress =
        if (goal.target > 0) {
            (goal.saved / goal.target).coerceIn(0.0, 1.0)
        } else {
            0.0
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = goal.name,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = FinanceGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = FinanceGreen,
                trackColor = FinanceBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${money(goal.saved)} de ${money(goal.target)}",
                color = FinanceGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onAdd
            ) {
                Text("Agregar $50")
            }
        }
    }
}

@Composable
fun NewGoalDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Nueva meta")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Nombre")
                    }
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = {
                        target = it
                    },
                    label = {
                        Text("Objetivo")
                    }
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val value = target.toDoubleOrNull()

                    if (name.isNotBlank() && value != null && value > 0) {

                        onSave(
                            name,
                            value
                        )
                    }
                }
            ) {
                Text("Crear")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun StatsScreen(vm: FinanceViewModel) {

    val total = vm.income + vm.expenses

    val expensePercentage =
        if (total > 0) {
            (vm.expenses / total).coerceIn(0.0, 1.0)
        } else {
            0.0
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                text = "Estadísticas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = FinanceBlue
            )

            Text(
                text = "Visualiza el comportamiento de tu dinero",
                color = FinanceGray
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Ingresos vs gastos",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ingresos: ${money(vm.income)}",
                        color = FinanceGreen
                    )

                    Text(
                        text = "Gastos: ${money(vm.expenses)}",
                        color = FinanceRed
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = {
                            expensePercentage.toFloat()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = FinanceRed
                    )
                }
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Balance",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = money(vm.balance),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (vm.balance >= 0) {
                            FinanceGreen
                        } else {
                            FinanceRed
                        }
                    )
                }
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Próximamente",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Aquí incorporaremos gráficos reales por mes, categorías, evolución del ahorro y deuda."
                    )
                }
            }
        }
    }
}

@Composable
fun DebtScreen() {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                text = "Deudas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = FinanceBlue
            )

            Text(
                text = "Organiza y controla tus compromisos",
                color = FinanceGray
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = FinanceOrange
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Control de deudas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "En la próxima versión podrás registrar saldo, cuota, intereses y abonos al capital."
                    )
                }
            }
        }
    }
}

@Composable
fun SalaryScreen() {

    var salary by remember { mutableStateOf("") }

    var mode by remember {
        mutableStateOf("Quincenal")
    }

    val value = salary.toDoubleOrNull() ?: 0.0

    val monthly =
        if (mode == "Quincenal") {
            value * 2
        } else {
            value
        }

    val biweekly =
        monthly / 2.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                text = "Mi salario",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = FinanceBlue
            )

            Text(
                text = "Calcula cuánto recibes y planifica mejor",
                color = FinanceGray
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "¿Cómo recibes tu salario?",
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected = mode == "Quincenal",
                            onClick = {
                                mode = "Quincenal"
                            },
                            label = {
                                Text("Quincenal")
                            }
                        )

                        FilterChip(
                            selected = mode == "Mensual",
                            onClick = {
                                mode = "Mensual"
                            },
                            label = {
                                Text("Mensual")
                            }
                        )
                    }

                    OutlinedTextField(
                        value = salary,
                        onValueChange = {
                            salary = it
                        },
                        label = {
                            Text("Monto")
                        },
                        singleLine = true
                    )
                }
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FinanceBlue
                )
            ) {

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    Text(
                        text = "Estimación",
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mensual: ${money(monthly)}",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Quincenal: ${money(biweekly)}",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FinanceGray,
                modifier = Modifier.size(55.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                color = FinanceGray
            )
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MainTheme {

                val vm: FinanceViewModel = viewModel()

                App(vm)
            }
        }
    }
}      
