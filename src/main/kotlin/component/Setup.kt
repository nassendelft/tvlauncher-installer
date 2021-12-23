package component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import dadb.Dadb

@Composable
fun Setup(
  onFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  val states = remember { mutableStateListOf<State>(State.PreRequisites) }
  val currentState = states.last()

  Column(
    modifier = modifier.padding(20.dp)
  ) {
    BreadCrumb(
      states = states
    ) {
      states.removeRange(states.indexOf(it) + 1, states.size)
    }
    Spacer(
      modifier = Modifier.height(20.dp)
    )
    when (currentState) {
      is State.PreRequisites -> Prerequisites { states.add(State.DeviceSelection) }
      is State.DeviceSelection -> DeviceSelection({ states.add(State.Installation(it)) })
      is State.Installation -> Installation(currentState.dadb, { states.add(State.Completion(it)) })
      is State.Completion -> Completion(currentState.dadb, onFinished)
    }
  }
}

@Composable
private fun BreadCrumb(
  states: List<State>,
  modifier: Modifier = Modifier,
  onStateClick: (State) -> Unit,
) {
  Row(
    modifier = modifier
  ) {
    for ((index, state) in states.withIndex()) {
      Text(
        text = state.title,
        style = MaterialTheme.typography.subtitle2,
        modifier = Modifier.then(
          if (index < states.size - 1) {
            Modifier.clickable { onStateClick(state) }
              .alpha(0.5f)
          } else Modifier
        )
      )
      if (index < states.size - 1) {
        Text(
          text = " > ",
          style = MaterialTheme.typography.subtitle2,
        )
      }
    }
  }
}

private sealed class State(val title: String) {
  object PreRequisites : State("Prerequisites")
  object DeviceSelection : State("Device selection")
  class Installation(val dadb: Dadb) : State("Installation")
  class Completion(val dadb: Dadb) : State("Completion")
}
