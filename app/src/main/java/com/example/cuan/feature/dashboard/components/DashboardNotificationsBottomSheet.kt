package com.example.cuan.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary
import com.example.cuan.ui.theme.Accent

// Data class representing a local notifications item
 
data class LocalNotificationItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val time: String
)

// Bottom sheet content that lists dynamic system and status notifications
 
@Composable
fun DashboardNotificationsBottomSheet(
    sheetsUrl: String,
    dailyReminderEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val notifications = mutableListOf<LocalNotificationItem>()

    // Notification 1: Sheets Connection
    if (sheetsUrl.isNotEmpty()) {
        notifications.add(
            LocalNotificationItem(
                title = "Google Sheets Terhubung",
                description = "Seluruh pencatatan Anda disinkronkan secara otomatis ke spreadsheet milik Anda.",
                icon = Icons.Default.TableChart,
                iconColor = Secondary,
                time = "Aktif"
            )
        )
    } else {
        notifications.add(
            LocalNotificationItem(
                title = "Spreadsheet Belum Terhubung",
                description = "Hubungkan Google Spreadsheet di Profil Anda untuk mencadangkan data secara aman.",
                icon = Icons.Default.WifiOff,
                iconColor = Accent,
                time = "Penting"
            )
        )
    }

    // Notification 2: Daily Reminder Status
    if (dailyReminderEnabled) {
        notifications.add(
            LocalNotificationItem(
                title = "Reminder Harian Aktif",
                description = "Anda akan menerima notifikasi setiap jam 9 malam apabila hari ini belum mencatat transaksi.",
                icon = Icons.Default.Notifications,
                iconColor = Secondary,
                time = "Setiap 21:00"
            )
        )
    } else {
        notifications.add(
            LocalNotificationItem(
                title = "Reminder Harian Nonaktif",
                description = "Aktifkan pengingat harian di menu Setelan agar pencatatan keuangan Anda lebih konsisten.",
                icon = Icons.Default.Notifications,
                iconColor = TextSecondary,
                time = "Info"
            )
        )
    }

    // Notification 3: General System Notification
    notifications.add(
        LocalNotificationItem(
            title = "Selamat Datang di CuOne!",
            description = "Mulai kelola keuangan Anda secara cerdas dengan asisten AI dan sistem pencatatan sovereign.",
            icon = Icons.Default.Settings,
            iconColor = Secondary,
            time = "Sistem"
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Notifikasi",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = OnBackground
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(notifications.size) { index ->
                val item = notifications[index]
                Card(
                    colors = CardDefaults.cardColors(containerColor = BackgroundVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(item.iconColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
