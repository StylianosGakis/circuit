package com.slack.circuit.shared.shapes.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

internal data class ShapeDefinition(
  val shape: Shape,
  val color: Color,
  val name: String,
) {
  val id: String = name
}