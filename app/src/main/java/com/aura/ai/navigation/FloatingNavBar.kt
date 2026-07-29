package com.aura.ai.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Floating glassmorphic bottom navigation bar. */
@Composable
fun FloatingNavBar(
    currentRoute: String?,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopLevelDestination.entries.forEach { dest ->
                NavItem(
                    dest = dest,
                    selected = currentRoute == dest.route,
                    onClick = { onNavigate(dest) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(dest: TopLevelDestination, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.1f else 1f, label = "navScale")
    val color by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        label = "navColor"
    )
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .semantics { contentDescription = dest.label }
    ) {
        Icon(dest.icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp).scale(scale))
        if (selected) {
            Text(dest.label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
