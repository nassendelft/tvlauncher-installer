package component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp


object Theme {

  val appLightColors = lightColors(
    primary = Color(0xFFff7043),
    secondary = Color(0xFF02CCEE),
    onPrimary = Color.Black,
  )

  val appDarkColors = darkColors(
    primary = Color(0xFFff7043),
    secondary = Color(0xFF02CCEE),
    onPrimary = Color.Black,
  )

  @Composable
  fun appBackgroundLight() = Brush.verticalGradient(
    colors = listOf(
      Color(0xFFDEDEDE),
      MaterialTheme.colors.background,
      Color(0xFFDEDEDE),
    )
  )

  @Composable
  fun appBackgroundDark() = Brush.verticalGradient(
    colors = listOf(
      MaterialTheme.colors.background,
      Color(0xFF4D4D4D),
      MaterialTheme.colors.background,
    )
  )

  @Composable
  fun appTypography() = MaterialTheme.typography.let {
    it.copy(
      h1 = it.h1.copy(fontFamily = Montserrat),
      h2 = it.h2.copy(fontFamily = Montserrat),
      h3 = it.h3.copy(fontFamily = Montserrat),
      h4 = it.h4.copy(fontFamily = Montserrat),
      h5 = it.h5.copy(fontFamily = Montserrat, fontWeight = FontWeight.W600, fontSize = 16.sp),
      h6 = it.h6.copy(fontFamily = Montserrat, fontWeight = FontWeight.W600, fontSize = 14.sp),
      subtitle1 = it.subtitle1.copy(fontFamily = Montserrat, fontSize = 13.sp),
      subtitle2 = it.subtitle2.copy(
        fontFamily = Montserrat,
        fontWeight = FontWeight.W600,
        fontSize = 10.sp
      ),
      body1 = it.body1.copy(fontFamily = Montserrat, fontSize = 13.sp),
      body2 = it.body2.copy(fontFamily = Montserrat, fontSize = 13.sp),
      button = it.button.copy(fontFamily = Montserrat),
      caption = it.caption.copy(fontFamily = Montserrat),
      overline = it.overline.copy(fontFamily = Montserrat),
    )
  }

  private val Montserrat = FontFamily(
    Font(
      resource = "font/montserrat_semi_bold.ttf",
      weight = FontWeight.W600,
      style = FontStyle.Normal
    ),
    Font(
      resource = "font/montserrat_regular.ttf",
      weight = FontWeight.W400,
      style = FontStyle.Normal
    ),
  )
}

@Composable
fun OutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  elevation: ButtonElevation? = null,
  shape: Shape = MaterialTheme.shapes.small,
  border: BorderStroke? = ButtonDefaults.outlinedBorder,
  colors: ButtonColors = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colors.onSurface
  ),
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
  content: @Composable RowScope.() -> Unit
) = Button(
  onClick = onClick,
  modifier = modifier,
  enabled = enabled,
  interactionSource = interactionSource,
  elevation = elevation,
  shape = shape,
  border = border,
  colors = colors,
  contentPadding = contentPadding,
  content = content
)

@Composable
fun TextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions(),
  singleLine: Boolean = false,
  maxLines: Int = Int.MAX_VALUE,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  shape: Shape = MaterialTheme.shapes.small.copy(ZeroCornerSize),
  colors: TextFieldColors = TextFieldDefaults.textFieldColors()
) {
  androidx.compose.material.TextField(
    enabled = enabled,
    readOnly = readOnly,
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = singleLine,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    isError = isError,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    maxLines = maxLines,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors
  )
}
