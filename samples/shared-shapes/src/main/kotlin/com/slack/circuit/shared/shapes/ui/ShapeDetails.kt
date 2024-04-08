package com.slack.circuit.shared.shapes.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.slack.circuit.foundation.LocalCircuitAnimatedContentScope
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.shared.shapes.LocalSharedTransitionScope
import com.slack.circuit.shared.shapes.data.ShapesRepository
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ShapeDetailsScreen(
  val id: String,
) : Screen {
  data class State(
    val id: String,
    val name: String,
    val shape: Shape,
    val color: Color,
  ) : CircuitUiState
}

@Composable
internal fun ShapeDetailsUi(state: ShapeDetailsScreen.State, modifier: Modifier = Modifier) {
  Column(
    modifier.fillMaxSize(),
    Arrangement.spacedBy(32.dp),
    Alignment.CenterHorizontally
  ) {
    with(LocalSharedTransitionScope.current) {
      Box(
        Modifier
          .sharedElement(
            rememberSharedContentState(state.id),
            LocalCircuitAnimatedContentScope.current,
          )
          .size(250.dp)
          .clip(state.shape)
          .background(state.color)
      )
      Text(state.name)
    }
  }
}

internal data class ShapeDetailsPresenter(
  private val id: String,
  private val shapesRepository: ShapesRepository,
) : Presenter<ShapeDetailsScreen.State> {
  @Composable
  override fun present(): ShapeDetailsScreen.State {
    val shapeDefinition = remember { shapesRepository.getShape(id) }
    return ShapeDetailsScreen.State(
      id = shapeDefinition.id,
      name = shapeDefinition.name,
      shape = shapeDefinition.shape,
      color = shapeDefinition.color,
    )
  }
}
