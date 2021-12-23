package component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

sealed class ActionState<T> {
  class Initial<T>: ActionState<T>()
  class Executing<T>: ActionState<T>()
  class Successful<T>(val value: T): ActionState<T>()
  class Failed<T>(val message: String? = null): ActionState<T>()
}

@Composable
fun <T> rememberActionState() =
  remember { mutableStateOf<ActionState<T>>(ActionState.Initial()) }
