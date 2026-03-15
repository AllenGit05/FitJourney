package com.example.fitjourneyag.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.fitjourneyag.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FJTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = FJTextSecondary) },
        leadingIcon = leadingIcon,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = FJGold,
            unfocusedBorderColor = FJDivider,
            focusedContainerColor   = FJSurface,
            unfocusedContainerColor = FJSurface,
            focusedTextColor   = FJTextPrimary,
            unfocusedTextColor = FJTextPrimary,
            cursorColor        = FJGold
        )
    )
}

@Composable
fun FJSectionLabel(label: String) {
    Text(
        text = label,
        color = FJGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    HorizontalDivider(color = FJDivider, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun FJOptionLabel(label: String) {
    Text(
        text = label,
        color = FJTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FJOptionRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    SimpleFlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Button(
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) FJGold else FJSurfaceHigh,
                    contentColor   = if (isSelected) FJOnGold else FJTextSecondary
                )
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun SimpleFlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hSpacingPx = horizontalSpacing.roundToPx()
        val vSpacingPx = verticalSpacing.roundToPx()

        var currentX = 0
        var currentY = 0
        var rowHeight = 0
        
        val itemPositions = mutableListOf<Triple<Int, Int, androidx.compose.ui.layout.Placeable>>()
        
        for (measurable in measurables) {
            val placeable = measurable.measure(constraints)
            if (currentX + placeable.width > constraints.maxWidth && currentX > 0) {
                currentX = 0
                currentY += rowHeight + vSpacingPx
                rowHeight = 0
            }
            itemPositions.add(Triple(currentX, currentY, placeable))
            currentX += placeable.width + hSpacingPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }
        
        val height = if (itemPositions.isEmpty()) 0 else currentY + rowHeight
        
        layout(constraints.maxWidth, height) {
            for ((x, y, placeable) in itemPositions) {
                placeable.placeRelative(x = x, y = y)
            }
        }
    }
}
@Composable
fun CreditStoreDialog(isOutOfCredits: Boolean = false, onDismiss: () -> Unit, onPurchase: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { 
            Text(
                if (isOutOfCredits) "Credits Depleted!" else "AI Credit Store", 
                color = FJGold, 
                fontWeight = FontWeight.Bold 
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (isOutOfCredits) 
                        "You've used your monthly allowance of 20 credits. Top up now to keep chatting with Aurora!" 
                    else 
                        "Refill your credits or go Elite for unlimited AI power. Every free user gets 20 credits/month automatically.", 
                    color = FJTextSecondary,
                    fontSize = 14.sp
                )
                
                StoreOption("Quick Refill", "50 Credits", "$4.99", "Ideal for daily tracking", onPurchase = { onPurchase("Starter") })
                StoreOption("Growth Pack", "200 Credits", "$14.99", "Perfect for serious goals", onPurchase = { onPurchase("Pro") })
                StoreOption("Elite Tier", "Unlimited", "$29.99/mo", "Zero limits, maximum gains", isPremium = true, onPurchase = { onPurchase("Elite") })
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Maybe Later", color = FJTextSecondary) }
        }
    )
}

@Composable
fun StoreOption(
    title: String,
    credits: String,
    price: String,
    description: String,
    isPremium: Boolean = false,
    onPurchase: () -> Unit
) {
    Surface(
        onClick = onPurchase,
        color = if (isPremium) FJGold.copy(alpha = 0.1f) else FJBackground,
        shape = RoundedCornerShape(12.dp),
        border = if (isPremium) androidx.compose.foundation.BorderStroke(1.dp, FJGold) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, color = FJTextSecondary, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(credits, color = FJGold, fontWeight = FontWeight.Bold)
                Text(price, color = FJTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun PaymentDialog(
    packageName: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = if (isProcessing) { {} } else onDismiss,
        containerColor = FJSurface,
        title = {
            Column {
                Text("Checkout", color = FJGold, fontWeight = FontWeight.Bold)
                Text("Safe & Secure Payment", color = FJTextSecondary, fontSize = 11.sp)
            }
        },
        text = {
            if (isProcessing) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = FJGold)
                    Text("Securing transaction...", color = FJTextSecondary)
                }
                LaunchedEffect(Unit) {
                    delay(2000)
                    onPaymentSuccess()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        color = FJBackground,
                        shape = RoundedCornerShape(12.dp),
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", color = FJTextSecondary, fontSize = 14.sp)
                            Text(if (packageName == "Starter") "$4.99" else if (packageName == "Pro") "$14.99" else "$29.99", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentSmallOption("Card", Icons.Default.CreditCard, selectedMethod == "Card") { selectedMethod = "Card" }
                        PaymentSmallOption("UPI", Icons.Default.QrCode, selectedMethod == "UPI") { selectedMethod = "UPI" }
                    }

                    if (selectedMethod == "Card") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16) cardNumber = it },
                                placeholder = { Text("Card Number", color = FJTextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = { Icon(Icons.Default.Lock, null, tint = FJTextSecondary, modifier = Modifier.size(16.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = expiry,
                                    onValueChange = { if (it.length <= 5) expiry = it },
                                    placeholder = { Text("MM/YY", color = FJTextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                                )
                                OutlinedTextField(
                                    value = cvv,
                                    onValueChange = { if (it.length <= 3) cvv = it },
                                    placeholder = { Text("CVV", color = FJTextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                                )
                            }
                        }
                    } else if (selectedMethod == "UPI") {
                         OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Enter UPI ID", color = FJTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Security, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PCI-DSS Compliant", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (!isProcessing) {
                val isEnabled = if (selectedMethod == "Card") {
                    cardNumber.length == 16 && expiry.length == 5 && cvv.length == 3
                } else if (selectedMethod == "UPI") {
                    true // mockup
                } else false

                Button(
                    onClick = { isProcessing = true },
                    enabled = isEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Secure Purchase", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun PaymentSmallOption(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(64.dp),
        color = if (isSelected) FJGold.copy(0.1f) else FJBackground,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FJGold else FJSurfaceHigh)
    ) {
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (isSelected) FJGold else FJTextSecondary, modifier = Modifier.size(24.dp))
            Text(title, color = if (isSelected) FJGold else FJTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
