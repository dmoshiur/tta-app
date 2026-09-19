package com.example.feature.explore

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.network.ExploreDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onItemClick: (String) -> Unit
) {
    val items by viewModel.exploreItems.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val types = listOf(
        ExploreType("ARTICLE", stringResource(id = R.string.tab_articles), Icons.Default.Article),
        ExploreType("BOOK", stringResource(id = R.string.tab_books), Icons.Default.Book),
        ExploreType("KNOWLEDGE", stringResource(id = R.string.tab_knowledge), Icons.Default.Lightbulb),
        ExploreType("WORLD", stringResource(id = R.string.tab_world), Icons.Default.Public),
        ExploreType("HUMANITY", stringResource(id = R.string.tab_humanity), Icons.Default.Favorite),
        ExploreType("SOCIETY", stringResource(id = R.string.tab_society), Icons.Default.Groups)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.nav_explore),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Explore
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchExplore(it) },
            placeholder = { Text(stringResource(id = R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_search_bar"),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Explore Category Type Chips
        ScrollableTabRow(
            selectedTabIndex = types.indexOfFirst { it.key == selectedType }.coerceAtLeast(0),
            edgePadding = 0.dp,
            divider = {},
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) {
            types.forEach { type ->
                val isSelected = selectedType == type.key
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.selectType(type.key) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = type.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            if (items.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No content found in this category.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items) { item ->
                        val isBookmarked = bookmarks.any { it.id == item.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item.id) }
                                .testTag("explore_item_${item.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.author} • ${item.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    IconButton(
                                        onClick = { viewModel.toggleBookmark(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.readingTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Read More",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreDetailScreen(
    viewModel: ExploreViewModel,
    itemId: String,
    onBackClick: () -> Unit
) {
    val selectedItem by viewModel.selectedItem.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(itemId) {
        viewModel.loadExploreDetail(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedItem?.type?.replace("_", " ") ?: "Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    selectedItem?.let { item ->
                        val isBookmarked = bookmarks.any { it.id == item.id }
                        IconButton(onClick = { viewModel.toggleBookmark(item) }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        selectedItem?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "By ${item.author} • Published ${item.date} • ${item.readingTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 26.sp
                )

                // Book Specific take-aways
                item.details?.let { details ->
                    if (details.keyIdeas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Key Takeaways",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        details.keyIdeas.forEach { idea ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("•", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(idea, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }

                    if (details.importantLessons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Practical Applications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        details.importantLessons.forEach { lesson ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("✔", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lesson, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }

                // Sources citations
                if (item.sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Sources & Citations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    item.sources.forEach { source ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            if (source.startsWith("http")) source else "https://scholar.google.com/scholar?q=${
                                                Uri.encode(
                                                    source
                                                )
                                            }"
                                        )
                                    )
                                    context.startActivity(intent)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(source, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open source link", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ExploreType(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
