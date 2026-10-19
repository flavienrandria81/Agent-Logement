package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminStats
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.PropertyWithDetails
import com.example.data.model.ReportStatus
import com.example.data.model.ReportWithDetails
import com.example.data.model.TransactionType
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EvolutionPeriod(val label: String, val days: Int) {
    WEEK("7 jours", 7),
    MONTH("30 jours", 30),
    SIX_MONTHS("6 mois", 180),
    ALL("Tout", 3650)
}

/**
 * Dashboard analytique moderne avec 4 graphiques professionnels interactifs:
 * 1. Graphique 1 — Évolution des annonces (Courbe Canvas avec gradient et filtre temporel)
 * 2. Graphique 2 — Répartition des biens (Anneau Donut Maisons, Appartements, Terrains, etc.)
 * 3. Graphique 3 — Location vs Vente (Barres comparatives, ratios et prix moyens réels)
 * 4. Graphique 4 — Activité des utilisateurs & Interactions (Rôles, visites, candidatures)
 */
@Composable
fun AdminAnalyticsDashboard(
    adminStats: AdminStats,
    allProperties: List<PropertyWithDetails>,
    reports: List<ReportWithDetails>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(CornerRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_analytics_dashboard")
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(CornerRadius.lg))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.sm),
                        color = DesignColors.PrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = DesignColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text = "Analytics & Analyses Avancées",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Graphiques décisionnels calculés en temps réel",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // ==========================================
            // GRAPHIQUE 1: Évolution des annonces (Courbe)
            // ==========================================
            AdEvolutionChart(properties = allProperties)

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // ==========================================
            // GRAPHIQUE 2: Répartition des biens (Donut)
            // ==========================================
            PropertyTypesDonutChart(adminStats = adminStats)

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // ==========================================
            // GRAPHIQUE 3: Location vs Vente (Barres)
            // ==========================================
            RentVsSaleComparisonChart(
                adminStats = adminStats,
                properties = allProperties
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // ==========================================
            // GRAPHIQUE 4: Activité Utilisateurs & Interactions
            // ==========================================
            UserRolesAndInteractionsChart(
                adminStats = adminStats,
                reports = reports
            )
        }
    }
}

/**
 * Graphique 1 — Courbe de création des annonces dans le temps avec filtres de période.
 */
@Composable
private fun AdEvolutionChart(properties: List<PropertyWithDetails>) {
    var selectedPeriod by remember { mutableStateOf(EvolutionPeriod.MONTH) }
    var hoveredPointIndex by remember { mutableStateOf<Int?>(null) }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedPeriod, properties.size) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    // Calcul des points de la courbe selon la période choisie
    val now = System.currentTimeMillis()
    val periodMillis = selectedPeriod.days * 24L * 60L * 60L * 1000L
    val startTime = now - periodMillis

    // Découpage en 7 intervalles temporels pour former la courbe
    val intervalCount = 7
    val stepMillis = periodMillis / intervalCount

    val dataPoints = remember(properties, selectedPeriod) {
        val points = mutableListOf<Pair<String, Int>>()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.FRANCE)

        for (i in 0..intervalCount) {
            val pointTime = startTime + (i * stepMillis)
            val windowStart = pointTime - (stepMillis / 2)
            val windowEnd = pointTime + (stepMillis / 2)

            val count = properties.count { item ->
                val created = item.property.createdAt
                if (selectedPeriod == EvolutionPeriod.ALL) {
                    created <= windowEnd
                } else {
                    created in windowStart..windowEnd
                }
            }.coerceAtLeast(0)

            val label = dateFormat.format(Date(pointTime))
            points.add(label to count)
        }
        points
    }

    val maxCount = (dataPoints.maxOfOrNull { it.second } ?: 1).coerceAtLeast(4)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = DesignColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "Évolution des annonces publiées",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Filtres de période interactifs
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                EvolutionPeriod.values().forEach { period ->
                    val isSelected = selectedPeriod == period
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.pill),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { selectedPeriod = period }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = period.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Surface Graphique Canvas avec courbe de Bézier et gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(CornerRadius.md)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingBottom = 22.dp.toPx()
                val chartHeight = height - paddingBottom
                val pointSpacing = width / (dataPoints.size - 1).coerceAtLeast(1)

                // Lignes de grille horizontales
                val gridLines = 3
                for (g in 0..gridLines) {
                    val y = chartHeight * (g.toFloat() / gridLines)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Points coordonnées
                val points = dataPoints.mapIndexed { index, pair ->
                    val x = index * pointSpacing
                    val normalizedY = 1f - (pair.second.toFloat() / maxCount)
                    val animatedY = chartHeight - ((chartHeight - (normalizedY * chartHeight)) * animProgress.value)
                    Offset(x, animatedY)
                }

                if (points.isNotEmpty()) {
                    // Tracé de la courbe lissée
                    val strokePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2, p1.y)
                            cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                        }
                    }

                    // Remplissage avec gradient
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(points.last().x, chartHeight)
                        lineTo(points.first().x, chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f * animProgress.value),
                                primaryColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Dessin des points circulaires
                    points.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // Labels des dates sur l'axe X
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEachIndexed { idx, pair ->
                    if (idx % 2 == 0 || idx == dataPoints.lastIndex) {
                        Text(
                            text = pair.first,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Graphique 2 — Diagramme en anneau pour maisons, appartements et terrains.
 */
@Composable
private fun PropertyTypesDonutChart(adminStats: AdminStats) {
    val houses = adminStats.housesCount.toFloat().coerceAtLeast(0f)
    val apartments = adminStats.apartmentsCount.toFloat().coerceAtLeast(0f)
    val lands = adminStats.landsCount.toFloat().coerceAtLeast(0f)
    val commercial = adminStats.commercialCount.toFloat().coerceAtLeast(0f)
    val total = (houses + apartments + lands + commercial).coerceAtLeast(1f)

    val houseColor = DesignColors.Secondary
    val aptColor = DesignColors.Primary
    val landColor = DesignColors.Warning
    val commColor = DesignColors.Accent

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(adminStats) {
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PieChart, contentDescription = null, tint = DesignColors.Secondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Répartition par Catégorie de Bien (${adminStats.totalProperties} biens)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Anneau Canvas Donut
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(126.dp)
                    .padding(4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val arcSize = Size(diameter, diameter)

                    var startAngle = -90f

                    val slices = listOf(
                        apartments / total to aptColor,
                        houses / total to houseColor,
                        lands / total to landColor,
                        commercial / total to commColor
                    )

                    slices.forEach { (fraction, color) ->
                        val sweep = fraction * 360f * animProgress.value
                        if (sweep > 0) {
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweep
                        }
                    }
                }

                // Chiffre central
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${adminStats.totalProperties}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Biens",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Légende dynamique avec décompte réel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartLegendItem(
                    label = "Appartements & Studios",
                    count = adminStats.apartmentsCount,
                    total = total.toInt(),
                    color = aptColor
                )
                ChartLegendItem(
                    label = "Maisons & Villas",
                    count = adminStats.housesCount,
                    total = total.toInt(),
                    color = houseColor
                )
                ChartLegendItem(
                    label = "Terrains constructibles",
                    count = adminStats.landsCount,
                    total = total.toInt(),
                    color = landColor
                )
                ChartLegendItem(
                    label = "Locaux commerciaux",
                    count = adminStats.commercialCount,
                    total = total.toInt(),
                    color = commColor
                )
            }
        }
    }
}

/**
 * Graphique 3 — Comparatif Location vs Vente avec barres et moyennes de prix constatées.
 */
@Composable
private fun RentVsSaleComparisonChart(
    adminStats: AdminStats,
    properties: List<PropertyWithDetails>
) {
    val rentCount = adminStats.forRentCount
    val saleCount = adminStats.forSaleCount
    val total = (rentCount + saleCount).coerceAtLeast(1)

    val rentPercent = (rentCount.toFloat() / total * 100).toInt()
    val salePercent = (saleCount.toFloat() / total * 100).toInt()

    // Moyennes réelles issues des données
    val rentProps = properties.filter { it.property.transactionType == TransactionType.RENT }
    val saleProps = properties.filter { it.property.transactionType == TransactionType.SALE }

    val avgRentPrice = if (rentProps.isNotEmpty()) rentProps.map { it.property.price }.average().toInt() else 0
    val avgSalePrice = if (saleProps.isNotEmpty()) saleProps.map { it.property.price }.average().toInt() else 0

    val currencyFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE)

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(adminStats) {
        animProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Sell, contentDescription = null, tint = DesignColors.Primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Comparatif Transaction : Location vs Vente",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Barre comparative empilée fluide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(CornerRadius.pill))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val rentWeight = ((rentCount.toFloat() / total) * animProgress.value).coerceAtLeast(0.01f)
            val saleWeight = ((saleCount.toFloat() / total) * animProgress.value).coerceAtLeast(0.01f)

            Box(
                modifier = Modifier
                    .weight(rentWeight)
                    .fillMaxHeight()
                    .background(DesignColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                if (rentPercent >= 15) {
                    Text(
                        text = "Location $rentPercent%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(saleWeight)
                    .fillMaxHeight()
                    .background(DesignColors.Secondary),
                contentAlignment = Alignment.Center
            ) {
                if (salePercent >= 15) {
                    Text(
                        text = "Vente $salePercent%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Cartes récapitulatives des moyennes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Surface(
                shape = RoundedCornerShape(CornerRadius.md),
                color = DesignColors.PrimaryContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, DesignColors.Primary.copy(alpha = 0.2f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = DesignColors.Primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Location", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DesignColors.Primary)
                    }
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = "$rentCount biens ($rentPercent%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Loyer moyen : ${currencyFormatter.format(avgRentPrice)} €/mois",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(CornerRadius.md),
                color = DesignColors.SecondaryContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, DesignColors.Secondary.copy(alpha = 0.2f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sell, contentDescription = null, tint = DesignColors.Secondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DesignColors.Secondary)
                    }
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = "$saleCount biens ($salePercent%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Prix moyen : ${currencyFormatter.format(avgSalePrice)} €",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Graphique 4 — Répartition des rôles utilisateurs et activité globale des interactions.
 */
@Composable
private fun UserRolesAndInteractionsChart(
    adminStats: AdminStats,
    reports: List<ReportWithDetails>
) {
    val totalUsers = adminStats.totalUsersCount.coerceAtLeast(1)
    val regular = adminStats.regularUsersCount
    val owners = adminStats.ownersCount
    val agents = adminStats.agentsCount
    val admins = adminStats.adminsCount

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(adminStats) {
        animProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Group, contentDescription = null, tint = DesignColors.Primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Utilisateurs & Activité de la Plateforme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Barre empilée des rôles
        Text(
            text = "Répartition des ${adminStats.totalUsersCount} comptes inscrits",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.xs))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(CornerRadius.pill))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val regW = (regular.toFloat() / totalUsers * animProgress.value).coerceAtLeast(0.01f)
            val ownW = (owners.toFloat() / totalUsers * animProgress.value).coerceAtLeast(0.01f)
            val agtW = (agents.toFloat() / totalUsers * animProgress.value).coerceAtLeast(0.01f)
            val admW = (admins.toFloat() / totalUsers * animProgress.value).coerceAtLeast(0.01f)

            Box(modifier = Modifier.weight(regW).fillMaxHeight().background(DesignColors.Primary))
            Box(modifier = Modifier.weight(ownW).fillMaxHeight().background(DesignColors.Secondary))
            Box(modifier = Modifier.weight(agtW).fillMaxHeight().background(DesignColors.Warning))
            Box(modifier = Modifier.weight(admW).fillMaxHeight().background(DesignColors.Accent))
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Légende des rôles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoleLegendChip(label = "Candidats ($regular)", color = DesignColors.Primary)
            RoleLegendChip(label = "Propriétaires ($owners)", color = DesignColors.Secondary)
            RoleLegendChip(label = "Agents ($agents)", color = DesignColors.Warning)
            RoleLegendChip(label = "Admin ($admins)", color = DesignColors.Accent)
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Indicateurs clés d'activité et flux de dossiers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            ActivityMetricPill(
                title = "Demandes visite",
                count = "${adminStats.visitRequestsCount}",
                icon = Icons.Default.CalendarMonth,
                color = DesignColors.Secondary,
                modifier = Modifier.weight(1f)
            )
            ActivityMetricPill(
                title = "Candidatures / Achats",
                count = "${adminStats.rentalAndPurchaseRequestsCount}",
                icon = Icons.Default.Key,
                color = DesignColors.Primary,
                modifier = Modifier.weight(1f)
            )
            ActivityMetricPill(
                title = "Signalements",
                count = "${adminStats.totalReportsCount}",
                icon = Icons.Default.ReportProblem,
                color = DesignColors.Error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RoleLegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActivityMetricPill(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(CornerRadius.md),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.xs, horizontal = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = count,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ChartLegendItem(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "$count ($percentage%)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
