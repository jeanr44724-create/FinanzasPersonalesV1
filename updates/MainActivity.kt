package com.example.finanzaspersonales

import android.app.Application
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
// MODELOS
// ============================================================

data class Movement(
    val id: String = System.currentTimeMillis().toString(),
    val type: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    val expenseType: String = "variable",
    val recurrent: Boolean = false,
    val date: String = currentDate()
)

data class SavingGoal(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val target: Double,
    val saved: Double = 0.0
)

data class TaxConfig(
    val cssRate: Double = 9.75,
    val educationRate: Double = 1.25,

    val isrExemptLimit: Double = 11000.0,
    val isrSecondLimit: Double = 50000.0,

    val isrSecondRate: Double = 15.0,
    val isrThirdRate: Double = 25.0,

    val isrBaseAtSecondLimit: Double = 5850.0,

    // Se puede modificar desde la aplicación.
    val isrMonths: Double = 13.0
)

data class SavedSalary(
    val grossMonthly: Double = 0.0,
    val mode: String = "Mensual",
    val netMonthly: Double = 0.0,
    val netBiweekly: Double = 0.0
)

fun currentDate(): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date())
}

// ============================================================
// VIEWMODEL
// ============================================================

class FinanceViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs =
        application.getSharedPreferences(
            "foxfin_preferences",
            Application.MODE_PRIVATE
        )

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

    // --------------------------------------------------------
    // SALARIO / IMPUESTOS
    // --------------------------------------------------------

    var taxConfig by mutableStateOf(
        TaxConfig(
            cssRate = prefs.getFloat(
                "css_rate",
                9.75f
            ).toDouble(),

            educationRate = prefs.getFloat(
                "education_rate",
                1.25f
            ).toDouble(),

            isrExemptLimit = prefs.getFloat(
                "isr_exempt_limit",
                11000f
            ).toDouble(),

            isrSecondLimit = prefs.getFloat(
                "isr_second_limit",
                50000f
            ).toDouble(),

            isrSecondRate = prefs.getFloat(
                "isr_second_rate",
                15f
            ).toDouble(),

            isrThirdRate = prefs.getFloat(
                "isr_third_rate",
                25f
            ).toDouble(),

            isrBaseAtSecondLimit = prefs.getFloat(
                "isr_base_second",
                5850f
            ).toDouble(),

            isrMonths = prefs.getFloat(
                "isr_months",
                13f
            ).toDouble()
        )
    )
        private set

    var savedSalary by mutableStateOf(
        SavedSalary(
            grossMonthly = prefs.getFloat(
                "saved_gross",
                0f
            ).toDouble(),

            mode = prefs.getString(
                "saved_mode",
                "Mensual"
            ) ?: "Mensual",

            netMonthly = prefs.getFloat(
                "saved_net_monthly",
                0f
            ).toDouble(),

            netBiweekly = prefs.getFloat(
                "saved_net_biweekly",
                0f
            ).toDouble()
        )
    )
        private set

    val hasSavedSalary: Boolean
        get() = savedSalary.grossMonthly > 0

    // --------------------------------------------------------
    // INGRESOS / GASTOS
    // --------------------------------------------------------

    val income: Double
        get() = movements
            .filter { it.type == "income" }
            .sumOf { it.amount }

    val expenses: Double
        get() = movements
            .filter { it.type == "expense" }
            .sumOf { it.amount }

    val fixedExpenses: Double
        get() = movements
            .filter {
                it.type == "expense" &&
                    it.expenseType == "fixed"
            }
            .sumOf { it.amount }

    val variableExpenses: Double
        get() = movements
            .filter {
                it.type == "expense" &&
                    it.expenseType == "variable"
            }
            .sumOf { it.amount }

    val recurrentExpenses: Double
        get() = movements
            .filter {
                it.type == "expense" &&
                    it.recurrent
            }
            .sumOf { it.amount }

    val balance: Double
        get() = income - expenses

    fun addMovement(
        type: String,
        amount: Double,
        category: String,
        note: String,
        expenseType: String,
        recurrent: Boolean,
        date: String
    ) {
        movements = movements + Movement(
            type = type,
            amount = amount,
            category = category,
            note = note,
            expenseType = expenseType,
            recurrent = recurrent,
            date = date
        )
    }

    fun deleteMovement(id: String) {
        movements = movements.filterNot {
            it.id == id
        }
    }

    fun addGoal(
        name: String,
        target: Double
    ) {
        goals = goals + SavingGoal(
            name = name,
            target = target
        )
    }

    fun addToGoal(
        id: String,
        amount: Double
    ) {
        goals = goals.map {
            if (it.id == id) {
                it.copy(
                    saved = it.saved + amount
                )
            } else {
                it
            }
        }
    }

    // --------------------------------------------------------
    // GUARDAR SALARIO
    // --------------------------------------------------------

    fun saveSalary(
        grossMonthly: Double,
        mode: String,
        netMonthly: Double,
        netBiweekly: Double
    ) {

        savedSalary = SavedSalary(
            grossMonthly = grossMonthly,
            mode = mode,
            netMonthly = netMonthly,
            netBiweekly = netBiweekly
        )

        prefs.edit()
            .putFloat(
                "saved_gross",
                grossMonthly.toFloat()
            )
            .putString(
                "saved_mode",
                mode
            )
            .putFloat(
                "saved_net_monthly",
                netMonthly.toFloat()
            )
            .putFloat(
                "saved_net_biweekly",
                netBiweekly.toFloat()
            )
            .apply()
    }

    fun deleteSavedSalary() {

        savedSalary = SavedSalary()

        prefs.edit()
            .remove("saved_gross")
            .remove("saved_mode")
            .remove("saved_net_monthly")
            .remove("saved_net_biweekly")
            .apply()
    }

    // --------------------------------------------------------
    // GUARDAR CONFIGURACIÓN DE IMPUESTOS
    // --------------------------------------------------------

    fun saveTaxConfig(
        config: TaxConfig
    ) {

        taxConfig = config

        prefs.edit()
            .putFloat(
                "css_rate",
                config.cssRate.toFloat()
            )
            .putFloat(
                "education_rate",
                config.educationRate.toFloat()
            )
            .putFloat(
                "isr_exempt_limit",
                config.isrExemptLimit.toFloat()
            )
            .putFloat(
                "isr_second_limit",
                config.isrSecondLimit.toFloat()
            )
            .putFloat(
                "isr_second_rate",
                config.isrSecondRate.toFloat()
            )
            .putFloat(
                "isr_third_rate",
                config.isrThirdRate.toFloat()
            )
            .putFloat(
                "isr_base_second",
                config.isrBaseAtSecondLimit.toFloat()
            )
            .putFloat(
                "isr_months",
                config.isrMonths.toFloat()
            )
            .apply()
    }

    fun resetTaxConfig() {

        val defaultConfig =
            TaxConfig()

        saveTaxConfig(
            defaultConfig
        )
    }
}

// ============================================================
// COLORES
// ============================================================

private val FinanceBlue = Color(0xFF12355B)
private val FinanceBlue2 = Color(0xFF1E6091)
private val FinanceGreen = Color(0xFF16A085)
private val FinanceRed = Color(0xFFE74C3C)
private val FinanceOrange = Color(0xFFF39C12)
private val FinanceBackground = Color(0xFFF4F7FA)
private val FinanceGray = Color(0xFF6B7280)

// ============================================================
// UTILIDADES
// ============================================================

private fun money(
    value: Double
): String {
    return "$" + String.format(
        Locale.US,
        "%.2f",
        value
    )
}

// ============================================================
// CÁLCULO SALARIAL
// ============================================================

data class SalaryCalculation(
    val grossMonthly: Double,
    val grossBiweekly: Double,
    val cssMonthly: Double,
    val educationMonthly: Double,
    val isrMonthly: Double,
    val totalDeductionsMonthly: Double,
    val netMonthly: Double,
    val netBiweekly: Double,
    val annualGross: Double,
    val annualTaxable: Double,
    val annualIsr: Double
)

fun calculateSalary(
    inputValue: Double,
    mode: String,
    config: TaxConfig
): SalaryCalculation {

    val grossMonthly =
        if (mode == "Quincenal") {
            inputValue * 2.0
        } else {
            inputValue
        }

    val grossBiweekly =
        grossMonthly / 2.0

    // CSS mensual
    val cssMonthly =
        grossMonthly *
            (config.cssRate / 100.0)

    // Seguro educativo mensual
    val educationMonthly =
        grossMonthly *
            (config.educationRate / 100.0)

    // Proyección anual para ISR.
    // El número de meses es editable desde la aplicación.
    val annualGross =
        grossMonthly *
            config.isrMonths.coerceAtLeast(1.0)

    val annualCss =
        cssMonthly *
            config.isrMonths.coerceAtLeast(1.0)

    val annualEducation =
        educationMonthly *
            config.isrMonths.coerceAtLeast(1.0)

    /*
     * Para que el usuario pueda modificar la fórmula,
     * la base gravable se calcula después de descontar
     * CSS y seguro educativo.
     */
    val annualTaxable =
        (
            annualGross -
                annualCss -
                annualEducation
            ).coerceAtLeast(0.0)

    val annualIsr =
        when {

            annualTaxable <=
                config.isrExemptLimit -> {
                0.0
            }

            annualTaxable <=
                config.isrSecondLimit -> {

                (
                    annualTaxable -
                        config.isrExemptLimit
                    ) *
                    (
                        config.isrSecondRate /
                            100.0
                    )
            }

            else -> {

                config.isrBaseAtSecondLimit +
                    (
                        annualTaxable -
                            config.isrSecondLimit
                        ) *
                        (
                            config.isrThirdRate /
                                100.0
                        )
            }
        }

    // Se distribuye el ISR anual entre 12 meses
    // para mostrar una estimación mensual.
    val isrMonthly =
        annualIsr / 12.0

    val totalDeductionsMonthly =
        cssMonthly +
            educationMonthly +
            isrMonthly

    val netMonthly =
        (
            grossMonthly -
                totalDeductionsMonthly
            ).coerceAtLeast(0.0)

    val netBiweekly =
        netMonthly / 2.0

    return SalaryCalculation(
        grossMonthly = grossMonthly,
        grossBiweekly = grossBiweekly,
        cssMonthly = cssMonthly,
        educationMonthly = educationMonthly,
        isrMonthly = isrMonthly,
        totalDeductionsMonthly =
            totalDeductionsMonthly,
        netMonthly = netMonthly,
        netBiweekly = netBiweekly,
        annualGross = annualGross,
        annualTaxable = annualTaxable,
        annualIsr = annualIsr
    )
}

// ============================================================
// TEMA
// ============================================================

@Composable
fun MainTheme(
    content: @Composable () -> Unit
) {

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

// ============================================================
// APP
// ============================================================

@Composable
fun App(
    vm: FinanceViewModel
) {

    val navController =
        rememberNavController()

    val items = listOf(
        Triple(
            "inicio",
            "Inicio",
            Icons.Default.Home
        ),
        Triple(
            "movimientos",
            "Movimientos",
            Icons.Default.List
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
            Icons.Default.AccountBalance
        ),
        Triple(
            "salario",
            "Salario",
            Icons.Default.Payments
        )
    )

    Scaffold(
        containerColor = FinanceBackground,

        bottomBar = {

            NavigationBar {

                items.forEach { item ->

                    val selected =
                        navController
                            .currentBackStackEntryAsState()
                            .value
                            ?.destination
                            ?.route == item.first

                    NavigationBarItem(
                        selected = selected,

                        onClick = {

                            navController.navigate(
                                item.first
                            ) {

                                popUpTo(
                                    navController
                                        .graph
                                        .startDestinationId
                                ) {
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
                SalaryScreen(vm)
            }
        }
    }
}

// ============================================================
// HOME
// ============================================================

@Composable
fun HomeScreen(
    vm: FinanceViewModel
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceBackground)
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                text = "Foxfin",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = FinanceBlue
            )

            Text(
                text =
                    "Controla tu dinero de forma inteligente",
                color = FinanceGray
            )
        }

        item {
            BalanceCard(vm.balance)
        }

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                SummaryCard(
                    modifier =
                        Modifier.weight(1f),

                    title = "Ingresos",

                    value =
                        money(vm.income),

                    color =
                        FinanceGreen,

                    icon =
                        Icons.Default.TrendingUp
                )

                SummaryCard(
                    modifier =
                        Modifier.weight(1f),

                    title = "Gastos",

                    value =
                        money(vm.expenses),

                    color =
                        FinanceRed,

                    icon =
                        Icons.Default.TrendingDown
                )
            }
        }

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                SummaryCard(
                    modifier =
                        Modifier.weight(1f),

                    title = "Fijos",

                    value =
                        money(vm.fixedExpenses),

                    color =
                        FinanceBlue2,

                    icon =
                        Icons.Default.Home
                )

                SummaryCard(
                    modifier =
                        Modifier.weight(1f),

                    title = "Variables",

                    value =
                        money(vm.variableExpenses),

                    color =
                        FinanceOrange,

                    icon =
                        Icons.Default.ShoppingCart
                )
            }
        }

        item {
            SavingsCard(vm)
        }

        item {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            FinanceBlue
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "Resumen financiero",

                        color =
                            Color.White,

                        fontSize =
                            19.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            if (vm.balance >= 0) {
                                "Tienes ${money(vm.balance)} disponibles después de tus gastos."
                            } else {
                                "Tus gastos superan tus ingresos por ${money(-vm.balance)}."
                            },

                        color =
                            Color.White.copy(
                                alpha = 0.9f
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Gastos recurrentes: ${money(vm.recurrentExpenses)}",

                        color =
                            Color.White.copy(
                                alpha = 0.8f
                            ),

                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// BALANCE
// ============================================================

@Composable
fun BalanceCard(
    balance: Double
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(26.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    FinanceBlue
            )
    ) {

        Column(
            modifier =
                Modifier.padding(24.dp)
        ) {

            Text(
                text =
                    "Balance disponible",

                color =
                    Color.White.copy(
                        alpha = 0.8f
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    money(balance),

                color =
                    Color.White,

                fontSize =
                    34.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    "Ingresos menos gastos",

                color =
                    Color.White.copy(
                        alpha = 0.7f
                    )
            )
        }
    }
}

// ============================================================
// SUMMARY CARD
// ============================================================

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

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = title,
                color = FinanceGray,
                fontSize = 13.sp
            )

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ============================================================
// SAVINGS CARD
// ============================================================

@Composable
fun SavingsCard(
    vm: FinanceViewModel
) {

    val goal =
        vm.goals.firstOrNull()
            ?: return

    val progress =
        if (goal.target > 0) {
            (goal.saved / goal.target)
                .coerceIn(0.0, 1.0)
        } else {
            0.0
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text =
                            "Meta de ahorro",

                        color =
                            FinanceGray
                    )

                    Text(
                        text =
                            goal.name,

                        fontSize =
                            19.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Icon(
                    imageVector =
                        Icons.Default.Savings,

                    contentDescription =
                        null,

                    tint =
                        FinanceGreen
                )
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Text(
                text =
                    "${money(goal.saved)} de ${money(goal.target)}",

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            LinearProgressIndicator(
                progress = {
                    progress.toFloat()
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(9.dp),

                color =
                    FinanceGreen,

                trackColor =
                    FinanceBackground
            )
        }
    }
}

// ============================================================
// MOVIMIENTOS
// ============================================================

@Composable
fun MovementsScreen(
    vm: FinanceViewModel
) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor =
            FinanceBackground,

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showDialog = true
                },

                containerColor =
                    FinanceBlue
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Add,

                    contentDescription =
                        "Agregar movimiento"
                )
            }
        }

    ) { padding ->

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            item {

                Text(
                    text =
                        "Movimientos",

                    fontSize =
                        28.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FinanceBlue,

                    modifier =
                        Modifier.padding(
                            top = 16.dp,
                            bottom = 2.dp
                        )
                )

                Text(
                    text =
                        "Clasifica tus gastos para que Foxfin pueda analizarlos.",

                    color =
                        FinanceGray,

                    fontSize =
                        13.sp,

                    modifier =
                        Modifier.padding(
                            bottom = 10.dp
                        )
                )
            }

            if (vm.movements.isEmpty()) {

                item {

                    EmptyState(
                        icon =
                            Icons.Default.ReceiptLong,

                        title =
                            "No tienes movimientos",

                        message =
                            "Agrega tu primer ingreso o gasto."
                    )
                }

            } else {

                item {

                    MovementSummary(
                        vm = vm
                    )
                }

                items(
                    items =
                        vm.movements.reversed(),

                    key = {
                        it.id
                    }
                ) { movement ->

                    MovementCard(
                        movement =
                            movement,

                        onDelete = {
                            vm.deleteMovement(
                                movement.id
                            )
                        }
                    )
                }
            }

            item {
                Spacer(
                    modifier =
                        Modifier.height(80.dp)
                )
            }
        }
    }

    if (showDialog) {

        MovementDialog(
            onDismiss = {
                showDialog = false
            },

            onSave = {
                    type,
                    amount,
                    category,
                    note,
                    expenseType,
                    recurrent,
                    date ->

                vm.addMovement(
                    type =
                        type,

                    amount =
                        amount,

                    category =
                        category,

                    note =
                        note,

                    expenseType =
                        expenseType,

                    recurrent =
                        recurrent,

                    date =
                        date
                )

                showDialog = false
            }
        )
    }
}

@Composable
fun MovementSummary(
    vm: FinanceViewModel
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    "Resumen de gastos",

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                SummaryMini(
                    title =
                        "Fijos",

                    value =
                        money(vm.fixedExpenses),

                    color =
                        FinanceBlue2
                )

                SummaryMini(
                    title =
                        "Variables",

                    value =
                        money(vm.variableExpenses),

                    color =
                        FinanceOrange
                )

                SummaryMini(
                    title =
                        "Recurrentes",

                    value =
                        money(vm.recurrentExpenses),

                    color =
                        FinanceRed
                )
            }
        }
    }
}

@Composable
fun SummaryMini(
    title: String,
    value: String,
    color: Color
) {

    Column {

        Text(
            text = title,
            color = FinanceGray,
            fontSize = 12.sp
        )

        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MovementCard(
    movement: Movement,
    onDelete: () -> Unit
) {

    val isIncome =
        movement.type == "income"

    val color =
        if (isIncome) {
            FinanceGreen
        } else {
            FinanceRed
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    if (isIncome) {
                        Icons.Default.TrendingUp
                    } else {
                        Icons.Default.TrendingDown
                    },

                contentDescription =
                    null,

                tint =
                    color,

                modifier =
                    Modifier.size(30.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        movement.category,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        movement.date,

                    color =
                        FinanceGray,

                    fontSize =
                        12.sp
                )

                if (movement.type == "expense") {

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {

                        Surface(
                            shape =
                                RoundedCornerShape(50.dp),

                            color =
                                if (
                                    movement.expenseType ==
                                        "fixed"
                                ) {
                                    FinanceBlue2.copy(
                                        alpha = 0.12f
                                    )
                                } else {
                                    FinanceOrange.copy(
                                        alpha = 0.15f
                                    )
                                }
                        ) {

                            Text(
                                text =
                                    if (
                                        movement.expenseType ==
                                            "fixed"
                                    ) {
                                        "Fijo"
                                    } else {
                                        "Variable"
                                    },

                                color =
                                    if (
                                        movement.expenseType ==
                                            "fixed"
                                    ) {
                                        FinanceBlue2
                                    } else {
                                        FinanceOrange
                                    },

                                fontSize =
                                    11.sp,

                                modifier =
                                    Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                            )
                        }

                        if (movement.recurrent) {

                            Surface(
                                shape =
                                    RoundedCornerShape(50.dp),

                                color =
                                    FinanceRed.copy(
                                        alpha = 0.10f
                                    )
                            ) {

                                Text(
                                    text =
                                        "Recurrente",

                                    color =
                                        FinanceRed,

                                    fontSize =
                                        11.sp,

                                    modifier =
                                        Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                )
                            }
                        }
                    }
                }

                if (movement.note.isNotBlank()) {

                    Text(
                        text =
                            movement.note,

                        color =
                            FinanceGray,

                        fontSize =
                            12.sp
                    )
                }
            }

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text =
                        if (isIncome) {
                            "+${money(movement.amount)}"
                        } else {
                            "-${money(movement.amount)}"
                        },

                    color =
                        color,

                    fontWeight =
                        FontWeight.Bold
                )

                IconButton(
                    onClick =
                        onDelete
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.DeleteOutline,

                        contentDescription =
                            "Eliminar",

                        tint =
                            FinanceGray
                    )
                }
            }
        }
    }
}

// ============================================================
// MOVEMENT DIALOG
// ============================================================

@Composable
fun MovementDialog(
    onDismiss: () -> Unit,

    onSave: (
        String,
        Double,
        String,
        String,
        String,
        Boolean,
        String
    ) -> Unit
) {

    var type by remember {
        mutableStateOf("expense")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    var expenseType by remember {
        mutableStateOf("variable")
    }

    var recurrent by remember {
        mutableStateOf(false)
    }

    var date by remember {
        mutableStateOf(currentDate())
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Nuevo movimiento",

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),

                modifier =
                    Modifier.heightIn(
                        max = 480.dp
                    )
            ) {

                item {

                    Text(
                        text =
                            "Tipo",

                        fontWeight =
                            FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected =
                                type == "expense",

                            onClick = {
                                type = "expense"
                            },

                            label = {
                                Text("Gasto")
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.TrendingDown,

                                    contentDescription =
                                        null
                                )
                            }
                        )

                        FilterChip(
                            selected =
                                type == "income",

                            onClick = {
                                type = "income"
                            },

                            label = {
                                Text("Ingreso")
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.TrendingUp,

                                    contentDescription =
                                        null
                                )
                            }
                        )
                    }
                }

                item {

                    OutlinedTextField(
                        value =
                            amount,

                        onValueChange = {
                            amount = it
                        },

                        label = {
                            Text("Monto")
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }

                item {

                    OutlinedTextField(
                        value =
                            category,

                        onValueChange = {
                            category = it
                        },

                        label = {
                            Text("Categoría")
                        },

                        placeholder = {
                            Text(
                                "Ej. Alimentación"
                            )
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }

                if (type == "expense") {

                    item {

                        Text(
                            text =
                                "Tipo de gasto",

                            fontWeight =
                                FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            FilterChip(
                                selected =
                                    expenseType ==
                                        "fixed",

                                onClick = {
                                    expenseType =
                                        "fixed"
                                },

                                label = {
                                    Text("Fijo")
                                }
                            )

                            FilterChip(
                                selected =
                                    expenseType ==
                                        "variable",

                                onClick = {
                                    expenseType =
                                        "variable"
                                },

                                label = {
                                    Text("Variable")
                                }
                            )
                        }
                    }

                    item {

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked =
                                    recurrent,

                                onCheckedChange = {
                                    recurrent = it
                                }
                            )

                            Text(
                                text =
                                    "Es un gasto recurrente"
                            )
                        }
                    }
                }

                item {

                    OutlinedTextField(
                        value =
                            date,

                        onValueChange = {
                            date = it
                        },

                        label = {
                            Text("Fecha")
                        },

                        placeholder = {
                            Text("dd/MM/yyyy")
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }

                item {

                    OutlinedTextField(
                        value =
                            note,

                        onValueChange = {
                            note = it
                        },

                        label = {
                            Text("Nota")
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val value =
                        amount.toDoubleOrNull()

                    if (
                        value != null &&
                        value > 0 &&
                        category.isNotBlank() &&
                        date.isNotBlank()
                    ) {

                        onSave(
                            type,
                            value,
                            category,
                            note,
                            if (
                                type == "expense"
                            ) {
                                expenseType
                            } else {
                                "income"
                            },
                            if (
                                type == "expense"
                            ) {
                                recurrent
                            } else {
                                false
                            },
                            date
                        )
                    }
                }
            ) {

                Text("Guardar")
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text("Cancelar")
            }
        }
    )
}

// ============================================================
// AHORROS
// ============================================================

@Composable
fun SavingsScreen(
    vm: FinanceViewModel
) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor =
            FinanceBackground,

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showDialog = true
                },

                containerColor =
                    FinanceGreen
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Add,

                    contentDescription =
                        "Nueva meta"
                )
            }
        }

    ) { padding ->

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            item {

                Text(
                    text =
                        "Mis ahorros",

                    fontSize =
                        28.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FinanceBlue
                )

                Text(
                    text =
                        "Construye tus metas paso a paso",

                    color =
                        FinanceGray
                )
            }

            items(vm.goals) { goal ->

                GoalCard(
                    goal =
                        goal,

                    onAdd = {

                        vm.addToGoal(
                            goal.id,
                            50.0
                        )
                    }
                )
            }

            item {

                Spacer(
                    modifier =
                        Modifier.height(80.dp)
                )
            }
        }
    }

    if (showDialog) {

        NewGoalDialog(
            onDismiss = {
                showDialog = false
            },

            onSave = {
                    name,
                    target ->

                vm.addGoal(
                    name,
                    target
                )

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
            (goal.saved / goal.target)
                .coerceIn(0.0, 1.0)
        } else {
            0.0
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        goal.name,

                    fontSize =
                        19.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "${(progress * 100).toInt()}%",

                    color =
                        FinanceGreen,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            LinearProgressIndicator(
                progress = {
                    progress.toFloat()
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp),

                color =
                    FinanceGreen,

                trackColor =
                    FinanceBackground
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "${money(goal.saved)} de ${money(goal.target)}",

                color =
                    FinanceGray
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick =
                    onAdd
            ) {

                Text(
                    "Agregar $50"
                )
            }
        }
    }
}

@Composable
fun NewGoalDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var target by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text("Nueva meta")
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value =
                        name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text("Nombre")
                    }
                )

                OutlinedTextField(
                    value =
                        target,

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

                    val value =
                        target.toDoubleOrNull()

                    if (
                        name.isNotBlank() &&
                        value != null &&
                        value > 0
                    ) {

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
                onClick =
                    onDismiss
            ) {

                Text("Cancelar")
            }
        }
    )
}

// ============================================================
// ESTADÍSTICAS
// ============================================================

@Composable
fun StatsScreen(
    vm: FinanceViewModel
) {

    val total =
        vm.income + vm.expenses

    val expensePercentage =
        if (total > 0) {
            (vm.expenses / total)
                .coerceIn(0.0, 1.0)
        } else {
            0.0
        }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    FinanceBackground
                )
                .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                text =
                    "Estadísticas",

                fontSize =
                    28.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    FinanceBlue
            )

            Text(
                text =
                    "Analiza cómo estás utilizando tu dinero",

                color =
                    FinanceGray
            )
        }

        item {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "Distribución de gastos",

                        fontSize =
                            19.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )

                    Text(
                        text =
                            "Gastos fijos: ${money(vm.fixedExpenses)}",

                        color =
                            FinanceBlue2
                    )

                    Text(
                        text =
                            "Gastos variables: ${money(vm.variableExpenses)}",

                        color =
                            FinanceOrange
                    )

                    Text(
                        text =
                            "Gastos recurrentes: ${money(vm.recurrentExpenses)}",

                        color =
                            FinanceRed
                    )
                }
            }
        }

        item {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "Ingresos vs gastos",

                        fontSize =
                            19.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Ingresos: ${money(vm.income)}",

                        color =
                            FinanceGreen
                    )

                    Text(
                        text =
                            "Gastos: ${money(vm.expenses)}",

                        color =
                            FinanceRed
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    LinearProgressIndicator(
                        progress = {
                            expensePercentage.toFloat()
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(10.dp),

                        color =
                            FinanceRed
                    )
                }
            }
        }

        item {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "Balance",

                        fontSize =
                            19.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            money(vm.balance),

                        fontSize =
                            30.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            if (vm.balance >= 0) {
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
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "Próximamente",

                        fontSize =
                            19.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Los datos de tus movimientos ya están preparados para crear gráficos por mes, categorías y evolución del ahorro."
                    )
                }
            }
        }
    }
}

// ============================================================
// DEUDAS
// ============================================================

@Composable
fun DebtScreen() {

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    FinanceBackground
                )
                .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                text =
                    "Deudas",

                fontSize =
                    28.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    FinanceBlue
            )

            Text(
                text =
                    "Organiza y controla tus compromisos",

                color =
                    FinanceGray
            )
        }

        item {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(22.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.AccountBalance,

                        contentDescription =
                            null,

                        tint =
                            FinanceOrange
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            "Control de deudas",

                        fontSize =
                            20.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "En la próxima versión podrás registrar saldo, cuota, intereses y abonos al capital."
                    )
                }
            }
        }
    }
}

// ============================================================
// SALARIO
// ============================================================

@Composable
fun SalaryScreen(
    vm: FinanceViewModel
) {

    var salary by remember {
        mutableStateOf(
            if (vm.hasSavedSalary) {
                vm.savedSalary.grossMonthly.toString()
            } else {
                ""
            }
        )
    }

    var mode by remember {
        mutableStateOf(
            if (vm.hasSavedSalary) {
                vm.savedSalary.mode
            } else {
                "Mensual"
            }
        )
    }

    var showTaxSettings by remember {
        mutableStateOf(false)
    }

    var editingSavedSalary by remember {
        mutableStateOf(
            vm.hasSavedSalary
        )
    }

    val inputValue =
        salary.toDoubleOrNull() ?: 0.0

    val calculation =
        calculateSalary(
            inputValue = inputValue,
            mode = mode,
            config = vm.taxConfig
        )

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    FinanceBackground
                )
                .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                text =
                    "Mi salario",

                fontSize =
                    28.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    FinanceBlue
            )

            Text(
                text =
                    "Calcula tu salario bruto, descuentos e ingreso neto.",

                color =
                    FinanceGray
            )
        }

        // ----------------------------------------------------
        // SALARIO GUARDADO
        // ----------------------------------------------------

        if (vm.hasSavedSalary) {

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FinanceGreen
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column {

                                Text(
                                    text =
                                        "Salario guardado",

                                    color =
                                        Color.White.copy(
                                            alpha = 0.8f
                                        )
                                )

                                Text(
                                    text =
                                        money(
                                            vm.savedSalary.netMonthly
                                        ),

                                    color =
                                        Color.White,

                                    fontSize =
                                        28.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    text =
                                        "Neto mensual",

                                    color =
                                        Color.White.copy(
                                            alpha = 0.85f
                                        )
                                )
                            }

                            Icon(
                                imageVector =
                                    Icons.Default.CheckCircle,

                                contentDescription =
                                    null,

                                tint =
                                    Color.White,

                                modifier =
                                    Modifier.size(38.dp)
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            OutlinedButton(
                                onClick = {

                                    salary =
                                        vm.savedSalary
                                            .grossMonthly
                                            .toString()

                                    mode =
                                        vm.savedSalary.mode

                                    editingSavedSalary =
                                        true
                                },

                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor =
                                            Color.White
                                    )
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Edit,

                                    contentDescription =
                                        null
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(5.dp)
                                )

                                Text("Modificar")
                            }

                            OutlinedButton(
                                onClick = {
                                    vm.deleteSavedSalary()

                                    salary = ""
                                    mode = "Mensual"

                                    editingSavedSalary =
                                        false
                                },

                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor =
                                            Color.White
                                    )
                            ) {

                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }

        // ----------------------------------------------------
        // ENTRADA DE SALARIO
        // ----------------------------------------------------

        item {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                if (editingSavedSalary) {
                                    "Modificar salario"
                                } else {
                                    "Nuevo salario"
                                },

                            fontWeight =
                                FontWeight.Bold,

                            fontSize =
                                19.sp
                        )

                        Icon(
                            imageVector =
                                Icons.Default.Payments,

                            contentDescription =
                                null,

                            tint =
                                FinanceBlue
                        )
                    }

                    Text(
                        text =
                            "¿Cómo recibes tu salario?",

                        fontWeight =
                            FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected =
                                mode == "Quincenal",

                            onClick = {
                                mode = "Quincenal"
                            },

                            label = {
                                Text("Quincenal")
                            }
                        )

                        FilterChip(
                            selected =
                                mode == "Mensual",

                            onClick = {
                                mode = "Mensual"
                            },

                            label = {
                                Text("Mensual")
                            }
                        )
                    }

                    OutlinedTextField(
                        value =
                            salary,

                        onValueChange = {
                            salary = it
                        },

                        label = {
                            Text(
                                if (
                                    mode ==
                                        "Quincenal"
                                ) {
                                    "Salario bruto quincenal"
                                } else {
                                    "Salario bruto mensual"
                                }
                            },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ----------------------------------------------------
        // DESGLOSE
        // ----------------------------------------------------

        if (inputValue > 0) {

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp)
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Text(
                            text =
                                "Desglose de descuentos",

                            fontSize =
                                19.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        SalaryLine(
                            title =
                                "Salario bruto mensual",

                            value =
                                money(
                                    calculation.grossMonthly
                                ),

                            color =
                                FinanceBlue
                        )

                        SalaryLine(
                            title =
                                "CSS (${formatRate(vm.taxConfig.cssRate)}%)",

                            value =
                                "-${money(calculation.cssMonthly)}",

                            color =
                                FinanceRed
                        )

                        SalaryLine(
                            title =
                                "Seguro educativo (${formatRate(vm.taxConfig.educationRate)}%)",

                            value =
                                "-${money(calculation.educationMonthly)}",

                            color =
                                FinanceRed
                        )

                        SalaryLine(
                            title =
                                "ISR mensual estimado",

                            value =
                                "-${money(calculation.isrMonthly)}",

                            color =
                                FinanceRed
                        )

                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    vertical = 10.dp
                                )
                        )

                        SalaryLine(
                            title =
                                "Total descuentos",

                            value =
                                "-${money(calculation.totalDeductionsMonthly)}",

                            color =
                                FinanceRed,

                            bold = true
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(16.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        FinanceGreen.copy(
                                            alpha = 0.10f
                                        )
                                )
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text =
                                        "SALARIO NETO",

                                    color =
                                        FinanceGreen,

                                    fontSize =
                                        13.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    text =
                                        money(
                                            calculation.netMonthly
                                        ),

                                    color =
                                        FinanceGreen,

                                    fontSize =
                                        30.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    text =
                                        "Quincenal: ${money(calculation.netBiweekly)}",

                                    color =
                                        FinanceGray
                                )
                            }
                        }
                    }
                }
            }

            // ------------------------------------------------
            // INFORMACIÓN ISR
            // ------------------------------------------------

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp)
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Text(
                            text =
                                "Cálculo anual del ISR",

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        SalaryLine(
                            title =
                                "Ingreso anual proyectado",

                            value =
                                money(
                                    calculation.annualGross
                                ),

                            color =
                                FinanceBlue
                        )

                        SalaryLine(
                            title =
                                "Base gravable estimada",

                            value =
                                money(
                                    calculation.annualTaxable
                                ),

                            color =
                                FinanceBlue2
                        )

                        SalaryLine(
                            title =
                                "ISR anual estimado",

                            value =
                                money(
                                    calculation.annualIsr
                                ),

                            color =
                                FinanceRed
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "El cálculo es una estimación configurable. Puedes modificar las tasas y tramos desde Configuración de impuestos.",

                            color =
                                FinanceGray,

                            fontSize =
                                12.sp
                        )
                    }
                }
            }
        }

        // ----------------------------------------------------
        // GUARDAR
        // ----------------------------------------------------

        item {

            Button(
                onClick = {

                    if (inputValue > 0) {

                        vm.saveSalary(
                            grossMonthly =
                                calculation.grossMonthly,

                            mode =
                                mode,

                            netMonthly =
                                calculation.netMonthly,

                            netBiweekly =
                                calculation.netBiweekly
                        )

                        editingSavedSalary =
                            false
                    }
                },

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp),

                enabled =
                    inputValue > 0
            ) {

                Icon(
                    imageVector =
                        if (vm.hasSavedSalary) {
                            Icons.Default.Save
                        } else {
                            Icons.Default.Save
                        },

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    if (vm.hasSavedSalary) {
                        "Guardar cambios"
                    } else {
                        "Guardar salario"
                    }
                )
            }
        }

        // ----------------------------------------------------
        // CONFIGURACIÓN DE IMPUESTOS
        // ----------------------------------------------------

        item {

            OutlinedButton(
                onClick = {
                    showTaxSettings = true
                },

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Settings,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    "Editar cálculos e impuestos"
                )
            }
        }

        item {

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )
        }
    }

    if (showTaxSettings) {

        TaxSettingsDialog(
            config =
                vm.taxConfig,

            onDismiss = {
                showTaxSettings = false
            },

            onSave = { config ->

                vm.saveTaxConfig(
                    config
                )

                showTaxSettings = false
            },

            onReset = {

                vm.resetTaxConfig()
            }
        )
    }
}

// ============================================================
// LÍNEA SALARIAL
// ============================================================

@Composable
fun SalaryLine(
    title: String,
    value: String,
    color: Color,
    bold: Boolean = false
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text =
                title,

            color =
                FinanceGray,

            fontWeight =
                if (bold) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
        )

        Text(
            text =
                value,

            color =
                color,

            fontWeight =
                FontWeight.Bold
        )
    }
}

fun formatRate(
    value: Double
): String {

    return String.format(
        Locale.US,
        "%.2f",
        value
    )
}

// ============================================================
// CONFIGURACIÓN DE IMPUESTOS
// ============================================================

@Composable
fun TaxSettingsDialog(
    config: TaxConfig,

    onDismiss: () -> Unit,

    onSave: (TaxConfig) -> Unit,

    onReset: () -> Unit
) {

    var css by remember {
        mutableStateOf(
            config.cssRate.toString()
        )
    }

    var education by remember {
        mutableStateOf(
            config.educationRate.toString()
        )
    }

    var exemptLimit by remember {
        mutableStateOf(
            config.isrExemptLimit.toString()
        )
    }

    var secondLimit by remember {
        mutableStateOf(
            config.isrSecondLimit.toString()
        )
    }

    var secondRate by remember {
        mutableStateOf(
            config.isrSecondRate.toString()
        )
    }

    var thirdRate by remember {
        mutableStateOf(
            config.isrThirdRate.toString()
        )
    }

    var baseSecond by remember {
        mutableStateOf(
            config.isrBaseAtSecondLimit.toString()
        )
    }

    var isrMonths by remember {
        mutableStateOf(
            config.isrMonths.toString()
        )
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Editar cálculos",

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),

                modifier =
                    Modifier.heightIn(
                        max = 520.dp
                    )
            ) {

                item {

                    Text(
                        text =
                            "Descuentos",

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            FinanceBlue
                    )
                }

                item {

                    TaxField(
                        value =
                            css,

                        onValueChange = {
                            css = it
                        },

                        label =
                            "CSS (%)"
                    )
                }

                item {

                    TaxField(
                        value =
                            education,

                        onValueChange = {
                            education = it
                        },

                        label =
                            "Seguro educativo (%)"
                    )
                }

                item {

                    HorizontalDivider()
                }

                item {

                    Text(
                        text =
                            "Impuesto sobre la renta",

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            FinanceBlue
                    )
                }

                item {

                    TaxField(
                        value =
                            exemptLimit,

                        onValueChange = {
                            exemptLimit = it
                        },

                        label =
                            "Límite exento ISR"
                    )
                }

                item {

                    TaxField(
                        value =
                            secondLimit,

                        onValueChange = {
                            secondLimit = it
                        },

                        label =
                            "Segundo límite ISR"
                    )
                }

                item {

                    TaxField(
                        value =
                            secondRate,

                        onValueChange = {
                            secondRate = it
                        },

                        label =
                            "Tasa segundo tramo (%)"
                    )
                }

                item {

                    TaxField(
                        value =
                            thirdRate,

                        onValueChange = {
                            thirdRate = it
                        },

                        label =
                            "Tasa tercer tramo (%)"
                    )
                }

                item {

                    TaxField(
                        value =
                            baseSecond,

                        onValueChange = {
                            baseSecond = it
                        },

                        label =
                            "Impuesto base a $50,000"
                    )
                }

                item {

                    TaxField(
                        value =
                            isrMonths,

                        onValueChange = {
                            isrMonths = it
                        },

                        label =
                            "Meses utilizados para proyección ISR"
                    )
                }

                item {

                    Text(
                        text =
                            "Los valores se guardan en el dispositivo y puedes modificarlos nuevamente cuando quieras.",

                        color =
                            FinanceGray,

                        fontSize =
                            12.sp
                    )
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val newConfig =
                        TaxConfig(

                            cssRate =
                                css.toDoubleOrNull()
                                    ?: config.cssRate,

                            educationRate =
                                education.toDoubleOrNull()
                                    ?: config.educationRate,

                            isrExemptLimit =
                                exemptLimit.toDoubleOrNull()
                                    ?: config.isrExemptLimit,

                            isrSecondLimit =
                                secondLimit.toDoubleOrNull()
                                    ?: config.isrSecondLimit,

                            isrSecondRate =
                                secondRate.toDoubleOrNull()
                                    ?: config.isrSecondRate,

                            isrThirdRate =
                                thirdRate.toDoubleOrNull()
                                    ?: config.isrThirdRate,

                            isrBaseAtSecondLimit =
                                baseSecond.toDoubleOrNull()
                                    ?: config.isrBaseAtSecondLimit,

                            isrMonths =
                                isrMonths.toDoubleOrNull()
                                    ?.coerceAtLeast(1.0)
                                    ?: config.isrMonths
                        )

                    onSave(
                        newConfig
                    )
                }
            ) {

                Text("Guardar cambios")
            }
        },

        dismissButton = {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                TextButton(
                    onClick = {
                        onReset()
                    }
                ) {

                    Text(
                        "Restaurar"
                    )
                }

                TextButton(
                    onClick =
                        onDismiss
                ) {

                    Text(
                        "Cancelar"
                    )
                }
            }
        }
    )
}

// ============================================================
// CAMPO DE IMPUESTO
// ============================================================

@Composable
fun TaxField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    OutlinedTextField(
        value =
            value,

        onValueChange =
            onValueChange,

        label = {
            Text(label)
        },

        singleLine = true,

        modifier =
            Modifier.fillMaxWidth()
    )
}

// ============================================================
// EMPTY STATE
// ============================================================

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 60.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    icon,

                contentDescription =
                    null,

                tint =
                    FinanceGray,

                modifier =
                    Modifier.size(55.dp)
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    title,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    message,

                color =
                    FinanceGray
            )
        }
    }
}

// ============================================================
// MAIN ACTIVITY
// ============================================================

class MainActivity :
    ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContent {

            MainTheme {

                val vm:
                    FinanceViewModel =
                    viewModel()

                App(vm)
            }
        }
    }
}
