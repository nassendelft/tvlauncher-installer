package component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Prerequisites(
  modifier: Modifier = Modifier,
  onFinished: () -> Unit
) {
  val step = remember { mutableStateOf<ShieldTvStep>(ShieldTvStep.Step1) }
  val btnPreviousEnabled = remember { mutableStateOf(false) }

  Column(
    modifier = modifier
  ) {
    Text(
      text = "Setup network debugging",
      style = typography.h5,
    )
    Spacer(
      modifier = Modifier.height(8.dp)
    )
    Text(
      text = "First you should enable 'Network debugging'.\n" +
        "This allows you to disable the default launcher and install your own.",
    )
    Spacer(
      modifier = Modifier.height(20.dp)
    )
    Column(
      modifier = Modifier
        .width(600.dp)
        .weight(1f)
        .padding(top = 4.dp)
        .align(Alignment.CenterHorizontally)
    ) {
      Image(
        painter = painterResource(step.value.step.imageResource),
        contentDescription = step.value.step.description,
        alignment = Alignment.Center
      )
      Spacer(
        modifier = Modifier.height(8.dp)
      )
      Text(
        text = "Step ${step.value.step.index}",
        style = typography.subtitle1,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )
      Spacer(
        modifier = Modifier.height(8.dp)
      )
      Text(
        text = step.value.step.description,
        textAlign = TextAlign.Center,
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )
    }
    Row(
      horizontalArrangement = Arrangement.Center,
    ) {
      OutlinedButton(
        onClick = {
          if (step.value == ShieldTvStep.Step2) {
            btnPreviousEnabled.value = false
          }
          val previousStep = step.value.previous()
          if (previousStep != null) {
            step.value = previousStep
          }
        },
        enabled = btnPreviousEnabled.value,
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "Previous",
          letterSpacing = 0.1.sp
        )
      }
      Spacer(
        modifier = Modifier.weight(4f)
      )
      Button(
        onClick = {
          val nextStep = step.value.next()
          if (nextStep == null) {
            onFinished()
          } else {
            step.value = nextStep
            btnPreviousEnabled.value = true
          }
        },
        modifier = Modifier.weight(1f),
      ) {
        Text(
          text = "Next",
          letterSpacing = 0.1.sp
        )
      }
    }
  }
}

private data class PrereqStep(
  val index: Int,
  val description: String,
  val imageResource: String
)

private sealed class ShieldTvStep(val step: PrereqStep) {
  object Step1 : ShieldTvStep(
    PrereqStep(
      index = 1,
      description = "Open Settings",
      imageResource = "/screenshots/shieldtv/screen0.png"
    )
  )

  object Step2 : ShieldTvStep(
    PrereqStep(
      index = 2,
      description = "Go to device settings",
      imageResource = "/screenshots/shieldtv/screen1.png"
    )
  )

  object Step3 : ShieldTvStep(
    PrereqStep(
      index = 3,
      description = "Select 'About'",
      imageResource = "/screenshots/shieldtv/screen2.png"
    )
  )

  object Step4 : ShieldTvStep(
    PrereqStep(
      index = 4,
      description = "Scroll down to 'Build' and press this 5 times. It should popup multiple " +
        "message with the final one saying 'You are now a developer'.",
      imageResource = "/screenshots/shieldtv/screen3.png"
    )
  )

  object Step5 : ShieldTvStep(
    PrereqStep(
      index = 5,
      description = "Now go back one screen and select 'Developer options",
      imageResource = "/screenshots/shieldtv/screen4.png"
    )
  )

  object Step6 : ShieldTvStep(
    PrereqStep(
      index = 6,
      description = "Scroll down and turn on 'Network debugging'. Please remember or note down " +
        "the number here, we are going to need it in the next step.",
      imageResource = "/screenshots/shieldtv/screen5.png"
    )
  )

  fun next() = when (this) {
    Step1 -> Step2
    Step2 -> Step3
    Step3 -> Step4
    Step4 -> Step5
    Step5 -> Step6
    Step6 -> null
  }

  fun previous() = when (this) {
    Step1 -> null
    Step2 -> Step1
    Step3 -> Step2
    Step4 -> Step3
    Step5 -> Step4
    Step6 -> Step5
  }
}

