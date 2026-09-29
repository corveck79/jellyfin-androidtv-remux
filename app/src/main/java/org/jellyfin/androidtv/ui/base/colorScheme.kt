package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ElegantFin-inspired palette (colors taken directly from the ElegantFin
// jellyfin-web theme's dark-mode CSS custom properties: --accentColor,
// --accentHoverColor, --tableBodyColor, --textColor, --dimTextColor, etc.)
// so the Android TV app's look matches the web client's "Mycelius Remux"
// theme instead of stock Jellyfin cyan/purple.
fun colorScheme(): ColorScheme = ColorScheme(
	background = Color(0xFF13161C),
	onBackground = Color(0xFFD1D5DB), // --textColor
	button = Color(0xFF4947CD), // --btnSubmitColor
	onButton = Color(0xFFD1D5DB),
	buttonFocused = Color(0xFF756FE2), // --accentHoverColor
	onButtonFocused = Color(0xFFFFFFFF),
	buttonDisabled = Color(0x334947CD),
	onButtonDisabled = Color(0xFF686868),
	buttonActive = Color(0xFF5D55E7), // --accentColor
	onButtonActive = Color(0xFFFFFFFF),
	input = Color(0xB3384252), // --selectorBackgroundColor
	onInput = Color(0xFFD1D5DB),
	inputFocused = Color(0xFF756FE2),
	onInputFocused = Color(0xFFFFFFFF),
	rangeControlBackground = Color(0xFF47505C), // --borderColor
	rangeControlFill = Color(0xFF5D55E7), // --accentColor
	rangeControlKnob = Color(0xFFD1D5DB),
	seekbarBuffer = Color(0xFF9CA3AF), // --dimTextColor
	recording = Color(0xFFEB6060), // --btnDeleteBorderColor
	onRecording = Color(0xFFFFFFFF),
	badge = Color(0xFF5D55E7), // --accentColor
	onBadge = Color(0xFFFFFFFF),
	listHeader = Color(0xFFD1D5DB),
	listOverline = Color(0xFF9CA3AF), // --dimTextColor
	listHeadline = Color(0xFFD1D5DB), // --textColor
	listCaption = Color(0xFF9CA3AF), // --dimTextColor
	listButton = Color.Transparent,
	listButtonFocused = Color(0xFF1F2937), // --tableBodyColor
	surface = Color(0xFF1F2937), // --tableBodyColor
	scrim = Color.Black.copy(alpha = 0.67f),
)

@Immutable
data class ColorScheme(
	val background: Color,
	val onBackground: Color,

	val button: Color,
	val onButton: Color,
	val buttonFocused: Color,
	val onButtonFocused: Color,
	val buttonDisabled: Color,
	val onButtonDisabled: Color,
	val buttonActive: Color,
	val onButtonActive: Color,

	val input: Color,
	val onInput: Color,
	val inputFocused: Color,
	val onInputFocused: Color,

	val rangeControlBackground: Color,
	val rangeControlFill: Color,
	val rangeControlKnob: Color,
	val seekbarBuffer: Color,

	val recording: Color,
	val onRecording: Color,

	val badge: Color,
	val onBadge: Color,

	val listHeader: Color,
	val listOverline: Color,
	val listHeadline: Color,
	val listCaption: Color,
	val listButton: Color,
	val listButtonFocused: Color,

	val surface: Color,
	val scrim: Color,
)

val LocalColorScheme = staticCompositionLocalOf { colorScheme() }
