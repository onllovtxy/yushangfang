package com.xixikitchen.jetpack.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.ui.noRippleCombinedPressable

enum class TicketTemplate(
    val code: String,
    val label: String,
    val subtitle: String,
    val recommendedRatio: CropAspectRatio
) {
    BOARDING_PASS("boarding_pass", "寻味登机牌", "蓝白航司票根", CropAspectRatio.RATIO_4_3),
    CINEMA_STUB("cinema_stub", "放映电影票", "复古暗金胶片", CropAspectRatio.RATIO_16_9),
    THERMAL_RECEIPT("thermal_receipt", "食堂小票", "锯齿热敏账单", CropAspectRatio.RATIO_1_1),
    TRAIN_EXPRESS("train_express", "浪漫列车票", "经典蓝底车票", CropAspectRatio.RATIO_4_3),
    POLAROID_POST("polaroid_post", "手帐明信片", "拍立得邮戳风", CropAspectRatio.RATIO_3_4);

    companion object {
        fun fromCode(code: String?): TicketTemplate =
            entries.firstOrNull { it.code == code } ?: BOARDING_PASS
    }
}

@Composable
fun TicketStubCard(
    diary: FoodDiary,
    previewBitmap: Bitmap? = null,
    onImageClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val template = TicketTemplate.fromCode(diary.templateCode)
    val ratio = CropAspectRatio.fromCode(diary.aspectRatio).ratio
    val dateLabel = remember(diary.createdAt) {
        diary.createdAt?.take(10)?.takeIf { it.isNotBlank() } ?: "TODAY"
    }
    val ticketNo = diary.ticketNo.ifBlank { "NO.2026-0520" }
    val title = diary.title.ifBlank { "今日份美味" }
    val location = diary.location.ifBlank { "熙熙的小厨房" }
    val moodTag = diary.moodTag.ifBlank { "超满足" }
    val passenger = diary.authorName?.takeIf { it.isNotBlank() } ?: "美食旅人"

    val clickableMod = if (onImageClick != null || onLongClick != null) {
        Modifier.noRippleCombinedPressable(
            pressedScale = 0.985f,
            onLongClick = onLongClick,
            onClick = { onImageClick?.invoke() }
        )
    } else {
        Modifier
    }

    Box(modifier = modifier.fillMaxWidth().then(clickableMod)) {
        when (template) {
            TicketTemplate.BOARDING_PASS -> BoardingPassTicket(
                imageUrl = diary.imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                title = title,
                content = diary.content,
                location = location,
                moodTag = moodTag,
                ticketNo = ticketNo,
                dateLabel = dateLabel,
                passenger = passenger
            )

            TicketTemplate.CINEMA_STUB -> CinemaStubTicket(
                imageUrl = diary.imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                title = title,
                content = diary.content,
                location = location,
                moodTag = moodTag,
                ticketNo = ticketNo,
                dateLabel = dateLabel
            )

            TicketTemplate.THERMAL_RECEIPT -> ThermalReceiptTicket(
                imageUrl = diary.imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                title = title,
                content = diary.content,
                location = location,
                moodTag = moodTag,
                ticketNo = ticketNo,
                dateLabel = dateLabel,
                passenger = passenger
            )

            TicketTemplate.TRAIN_EXPRESS -> TrainExpressTicket(
                imageUrl = diary.imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                title = title,
                content = diary.content,
                location = location,
                moodTag = moodTag,
                ticketNo = ticketNo,
                dateLabel = dateLabel
            )

            TicketTemplate.POLAROID_POST -> PolaroidPostTicket(
                imageUrl = diary.imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                title = title,
                content = diary.content,
                location = location,
                moodTag = moodTag,
                ticketNo = ticketNo,
                dateLabel = dateLabel,
                passenger = passenger
            )
        }
    }
}

@Composable
private fun TicketPhotoBox(
    imageUrl: String,
    previewBitmap: Bitmap?,
    ratio: Float,
    modifier: Modifier = Modifier
) {
    val imgBitmap = remember(previewBitmap) { previewBitmap?.asImageBitmap() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .background(Color(0xFFECEAE4))
    ) {
        if (imgBitmap != null) {
            Image(
                bitmap = imgBitmap,
                contentDescription = "票根美食照",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = realImageUrl(imageUrl),
                contentDescription = "票根美食照",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun BoardingPassTicket(
    imageUrl: String,
    previewBitmap: Bitmap?,
    ratio: Float,
    title: String,
    content: String,
    location: String,
    moodTag: String,
    ticketNo: String,
    dateLabel: String,
    passenger: String
) {
    val airlineBlue = Color(0xFF1D5C8B)
    val softSky = Color(0xFFF3F8FC)
    val ink = Color(0xFF1E293B)
    val muted = Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(BorderStroke(1.dp, Color(0xFFDCE7F2)), RoundedCornerShape(20.dp))
    ) {
        // 航司色带抬头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(airlineBlue)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "GOURMET BOARDING PASS",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "XX-520",
                color = Color(0xFFBAE6FD),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(softSky)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TicketPhotoBox(
                imageUrl = imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FROM",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = muted
                    )
                    Text(
                        text = "日常烟火",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = ink
                    )
                }
                Text(
                    text = "✈",
                    fontSize = 18.sp,
                    color = airlineBlue,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "DESTINATION",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = muted
                    )
                    Text(
                        text = location,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = airlineBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        PerforatedDivider(
            lineColor = Color(0xFFCBD5E1),
            notchColor = Color(0xFFE8EFF6)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetaCell("PASSENGER", passenger, Modifier.weight(1f), ink, muted)
                    MetaCell("DATE", dateLabel, Modifier.weight(1f), ink, muted)
                    MetaCell("CLASS", "头等美味", Modifier.weight(1f), airlineBlue, muted)
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = ink
                )

                if (content.isNotBlank()) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )
                }

                Spacer(Modifier.height(2.dp))
                BarcodeStrip(code = ticketNo, barColor = Color(0xFF1E293B))
            }

            InkStampBadge(
                text = moodTag,
                color = Color(0xFFD9534F),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 26.dp, end = 4.dp)
                    .rotate(-12f)
            )
        }
    }
}

@Composable
private fun CinemaStubTicket(
    imageUrl: String,
    previewBitmap: Bitmap?,
    ratio: Float,
    title: String,
    content: String,
    location: String,
    moodTag: String,
    ticketNo: String,
    dateLabel: String
) {
    val paper = Color(0xFFFBF6EC)
    val crimson = Color(0xFF8C2D27)
    val ink = Color(0xFF2B2118)
    val muted = Color(0xFF7D6B5D)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(paper)
            .border(BorderStroke(1.dp, Color(0xFFE7D9C1)), RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(crimson)
                .padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CINEMA DE CUISINE",
                color = Color(0xFFFDE68A),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "双人放映厅",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 胶片齿孔框
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1B18))
                .padding(vertical = 6.dp)
        ) {
            FilmSprocketsRow()
            TicketPhotoBox(
                imageUrl = imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
            FilmSprocketsRow()
        }

        PerforatedDivider(
            lineColor = Color(0xFFD6C3A5),
            notchColor = Color(0xFFEDE0CB)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "《$title》",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = ink
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetaCell("影厅", location, Modifier.weight(1.2f), ink, muted)
                    MetaCell("场次", dateLabel, Modifier.weight(1f), ink, muted)
                    MetaCell("座位", "05排20座", Modifier.weight(0.9f), crimson, muted)
                }
                if (content.isNotBlank()) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A3B31),
                        lineHeight = 20.sp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ADMIT TWO · $ticketNo",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = muted
                    )
                    InkStampBadge(
                        text = moodTag,
                        color = crimson,
                        modifier = Modifier.rotate(-7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThermalReceiptTicket(
    imageUrl: String,
    previewBitmap: Bitmap?,
    ratio: Float,
    title: String,
    content: String,
    location: String,
    moodTag: String,
    ticketNo: String,
    dateLabel: String,
    passenger: String
) {
    val paper = Color(0xFFFAFAF7)
    val ink = Color(0xFF18181B)
    val muted = Color(0xFF71717A)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(4.dp), clip = false)
            .background(paper)
            .border(BorderStroke(0.5.dp, Color(0xFFE4E4E7)))
    ) {
        SawtoothEdge(paperColor = Color(0xFFE5E5E0), isTop = true)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "*** XIXI BISTRO RECEIPT ***",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ink
            )
            Text(
                text = "DATE: $dateLabel   NO: $ticketNo",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = muted
            )

            DashedRule(color = Color(0xFFD4D4D8))

            TicketPhotoBox(
                imageUrl = imageUrl,
                previewBitmap = previewBitmap,
                ratio = ratio,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(BorderStroke(1.dp, Color(0xFFE4E4E7)), RoundedCornerShape(6.dp))
            )

            DashedRule(color = Color(0xFFD4D4D8))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ReceiptRow("ITEM: $title", "x1  ♥", ink, FontWeight.Bold)
                ReceiptRow("LOC : $location", passenger, muted, FontWeight.Normal)
                ReceiptRow("TAG : [$moodTag]", "100% SWEET", muted, FontWeight.Normal)
            }

            if (content.isNotBlank()) {
                DashedRule(color = Color(0xFFE4E4E7))
                Text(
                    text = content,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = ink,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DashedRule(color = Color(0xFFD4D4D8))

            ReceiptRow("TOTAL HAPPINESS", "PRICELESS", ink, FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            BarcodeStrip(code = "THANK YOU FOR LOVING ME", barColor = ink)
        }

        SawtoothEdge(paperColor = Color(0xFFE5E5E0), isTop = false)
    }
}

@Composable
private fun TrainExpressTicket(
    imageUrl: String,
    previewBitmap: Bitmap?,
    ratio: Float,
    title: String,
    content: String,
    location: String,
    moodTag: String,
    ticketNo: String,
    dateLabel: String
) {
    val ticketBlue = Color(0xFFE5F2F8)
    val navy = Color(0xFF1A4963)
    val redInk = Color(0xFFC93B3B)
    val muted = Color(0xFF537487)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(ticketBlue)
            .border(BorderStroke(1.dp, Color(0xFFB7D6E6)), RoundedCornerShape(16.dp))
    ) {
        // 车票顶部编码栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "K0520次  快乐特快",
                color = redInk,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "检票口: 幸福站",
                color = navy,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 站点栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "幸福日常",
                color = navy,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "──", color = muted, fontSize = 12.sp)
                Icon(
                    imageVector = Icons.Default.Train,
                    contentDescription = null,
                    tint = navy,
                    modifier = Modifier.padding(horizontal = 4.dp).size(16.dp)
                )
                Text(text = "──▶", color = muted, fontSize = 12.sp)
            }
            Text(
                text = location,
                color = navy,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(10.dp))

        TicketPhotoBox(
            imageUrl = imageUrl,
            previewBitmap = previewBitmap,
            ratio = ratio,
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(10.dp))

        PerforatedDivider(
            lineColor = Color(0xFF9AC2D8),
            notchColor = Color(0xFFD0E4F0)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = navy,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "￥0.00 元",
                        color = redInk,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                if (content.isNotBlank()) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF284657),
                        lineHeight = 20.sp
                    )
                }

                Text(
                    text = "$dateLabel 开 · 当日有效 · $ticketNo",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = muted
                )
            }

            InkStampBadge(
                text = moodTag,
                color = redInk,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 14.dp)
                    .rotate(10f)
            )
        }
    }
}

@Composable
private fun PolaroidPostTicket(
    imageUrl: String,
    previewBitmap: Bitmap?,
    ratio: Float,
    title: String,
    content: String,
    location: String,
    moodTag: String,
    ticketNo: String,
    dateLabel: String,
    passenger: String
) {
    val cream = Color(0xFFFFFDF7)
    val ink = Color(0xFF332D29)
    val muted = Color(0xFF8C7E74)
    val stampRed = Color(0xFFBE4B49)

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .shadow(6.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(cream)
                .border(BorderStroke(1.dp, Color(0xFFEBE3D5)), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FOOD POSTCARD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = muted
                    )
                    Text(
                        text = dateLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = ink
                    )
                }

                // 右上角复古邮票与邮戳
                Box(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, stampRed.copy(alpha = 0.7f)), RoundedCornerShape(4.dp))
                        .background(stampRed.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = moodTag,
                        color = stampRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 拍立得白边相框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(BorderStroke(1.dp, Color(0xFFECE5D8)))
                    .padding(8.dp)
            ) {
                TicketPhotoBox(
                    imageUrl = imageUrl,
                    previewBitmap = previewBitmap,
                    ratio = ratio
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic
                    ),
                    color = ink
                )
                Text(
                    text = "@ $location",
                    style = MaterialTheme.typography.labelMedium,
                    color = stampRed
                )
            }

            if (content.isNotBlank()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4B423C),
                    lineHeight = 20.sp
                )
            }

            DashedRule(color = Color(0xFFE5DDD0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "From $passenger",
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.sp,
                    color = muted
                )
                Text(
                    text = ticketNo,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = muted
                )
            }
        }

        // 顶部居中半透明和纸胶带装饰
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(74.dp)
                .height(18.dp)
                .rotate(-2f)
        ) {
            drawRect(color = Color(0xCCFFE2A8))
        }
    }
}

@Composable
private fun PerforatedDivider(
    lineColor: Color,
    notchColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
    ) {
        val r = size.height / 2f
        val y = size.height / 2f
        drawLine(
            color = lineColor,
            start = Offset(r + 6.dp.toPx(), y),
            end = Offset(size.width - r - 6.dp.toPx(), y),
            strokeWidth = 1.4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        )
        drawCircle(color = notchColor, radius = r, center = Offset(0f, y))
        drawCircle(color = notchColor, radius = r, center = Offset(size.width, y))
    }
}

@Composable
private fun FilmSprocketsRow() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .padding(horizontal = 10.dp)
    ) {
        val holeW = 8.dp.toPx()
        val holeH = 5.dp.toPx()
        val gap = 6.dp.toPx()
        var x = 0f
        val y = (size.height - holeH) / 2f
        while (x + holeW <= size.width) {
            drawRoundRect(
                color = Color(0xFFFBF6EC),
                topLeft = Offset(x, y),
                size = Size(holeW, holeH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            x += holeW + gap
        }
    }
}

@Composable
private fun SawtoothEdge(paperColor: Color, isTop: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
    ) {
        val toothW = 10.dp.toPx()
        val count = (size.width / toothW).toInt().coerceAtLeast(1)
        val step = size.width / count
        val path = Path()
        if (isTop) {
            path.moveTo(0f, 0f)
            for (i in 0 until count) {
                path.lineTo(i * step + step / 2f, size.height)
                path.lineTo((i + 1) * step, 0f)
            }
            path.lineTo(size.width, 0f)
        } else {
            path.moveTo(0f, size.height)
            for (i in 0 until count) {
                path.lineTo(i * step + step / 2f, 0f)
                path.lineTo((i + 1) * step, size.height)
            }
            path.lineTo(size.width, size.height)
        }
        path.close()
        drawPath(path, color = paperColor)
    }
}

@Composable
private fun BarcodeStrip(code: String, barColor: Color) {
    val pattern = remember(code) {
        val seed = code.hashCode()
        List(36) { idx ->
            val v = ((seed ushr (idx % 16)) xor (idx * 7)) and 0x3
            when (v) {
                0 -> 1.5f
                1 -> 3f
                2 -> 2f
                else -> 4f
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(22.dp)
        ) {
            val totalUnits = pattern.sum() + pattern.size * 1.8f
            val unitW = size.width / totalUnits
            var x = 0f
            pattern.forEach { w ->
                val barW = w * unitW
                drawRect(
                    color = barColor.copy(alpha = 0.85f),
                    topLeft = Offset(x, 0f),
                    size = Size(barW, size.height)
                )
                x += barW + 1.8f * unitW
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = barColor.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun InkStampBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.6.dp, color.copy(alpha = 0.78f)), RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color.copy(alpha = 0.88f),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MetaCell(
    label: String,
    value: String,
    modifier: Modifier,
    valueColor: Color,
    labelColor: Color
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = labelColor
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReceiptRow(
    left: String,
    right: String,
    color: Color,
    weight: FontWeight
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = left,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = weight,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = right,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = weight,
            color = color
        )
    }
}

@Composable
private fun DashedRule(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    }
}
