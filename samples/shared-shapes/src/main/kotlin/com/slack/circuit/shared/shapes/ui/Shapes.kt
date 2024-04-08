package com.slack.circuit.shared.shapes.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.slack.circuit.foundation.LocalCircuitAnimatedContentScope
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.shared.shapes.LocalSharedTransitionScope
import com.slack.circuit.shared.shapes.data.ShapesRepository
import com.slack.circuit.shared.shapes.model.ShapeDefinition
import kotlinx.parcelize.Parcelize

@Parcelize
internal data object ShapesScreen : Screen {
  data class State(
    val shapes: List<ShapeDefinition>,
    val onShapeClick: (ShapeDefinition) -> Unit,
  ) : CircuitUiState
}

internal class ShapesPresenter(
  private val navigator: Navigator,
  private val shapesRepository: ShapesRepository,
) : Presenter<ShapesScreen.State> {
  @Composable
  override fun present(): ShapesScreen.State {
    val shapes = shapesRepository.shapes
    return ShapesScreen.State(shapes) { shapeDefinition ->
      navigator.goTo(ShapeDetailsScreen(shapeDefinition.id))
    }
  }
}

@Composable
internal fun ShapesUi(state: ShapesScreen.State, modifier: Modifier = Modifier) {
  Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    for (shape in state.shapes) {
      ListItem(
        headlineContent = {
          with(LocalSharedTransitionScope.current) {
            Box(
              Modifier
                .sharedElement(
                  rememberSharedContentState(shape.id),
                  LocalCircuitAnimatedContentScope.current,
                )
                .size(80.dp)
                .clip(shape.shape)
                .background(shape.color)
            )
          }
        },
        modifier = Modifier.clickable { state.onShapeClick(shape) }
      )
    }
  }
}