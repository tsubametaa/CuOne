package com.example.cuan.feature.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.components.SecondaryButtonComponent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Onboarding Carousel - 3 slides introducing app features
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingCarouselScreen(
    onNavigateToName: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val slides = listOf(
        CarouselSlide(
            icon = android.R.drawable.ic_menu_edit,
            title = "Catat Setiap Transaksi",
            description = "Catat pemasukan dan pengeluaran dengan cepat, manual atau lewat scan struk"
        ),
        CarouselSlide(
            icon = android.R.drawable.ic_menu_camera,
            title = "Scan Struk dan QRIS",
            description = "Foto struk belanja atau screenshot QRIS, AI kami langsung mengenali Detailnya"
        ),
        CarouselSlide(
            icon = android.R.drawable.ic_menu_info_details,
            title = "Analitik Keuangan Cerdas",
            description = "Lihat grafik, deteksi pengeluaran tidak wajar, dan tanya langsung ke AI soal keuanganmu"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            CarouselSlideContent(slide = slides[page])
        }

        // Dot Indicators
        Row(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) Secondary
                            else BackgroundVariant
                        )
                )
            }
        }

        // Navigation Buttons
        if (pagerState.currentPage < 2) {
            // Next button (slides 1-2)
            PrimaryButtonComponent(
                text = "Berikutnya",
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Start button (slide 3)
            PrimaryButtonComponent(
                text = "Mulai",
                onClick = onNavigateToName,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class CarouselSlide(
    val icon: Int, // Use drawable resource ID
    val title: String,
    val description: String
)

@Composable
fun CarouselSlideContent(slide: CarouselSlide) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon (large, 64.dp)
        Icon(
            androidx.compose.ui.res.painterResource(slide.icon),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}