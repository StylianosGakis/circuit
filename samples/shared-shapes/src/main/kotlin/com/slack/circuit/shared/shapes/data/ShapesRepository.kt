package com.slack.circuit.shared.shapes.data

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.slack.circuit.shared.shapes.model.ShapeDefinition

internal object ShapesRepository {
  private val TriangleShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
      return Outline.Generic(
        Path().apply {
          moveTo(size.width / 2f, 0f)
          lineTo(0f, size.height)
          lineTo(size.width, size.height)
          close()
        }
      )
    }
  }
  val shapes = listOf(
    ShapeDefinition(CircleShape, Color.Red, "Red Circle"),
    ShapeDefinition(RectangleShape, Color.Green, "Green Rectangle"),
    ShapeDefinition(TriangleShape, Color.Blue, "Blue Triangle"),
    ShapeDefinition(CircleShape, Color.Yellow, "Yellow Circle"),
    ShapeDefinition(RectangleShape, Color.Cyan, "Cyan Rectangle"),
    ShapeDefinition(TriangleShape, Color.Magenta, "Magenta Triangle"),
    ShapeDefinition(CircleShape, Color.DarkGray, "DarkGray Circle"),
    ShapeDefinition(RectangleShape, Color.Gray, "Gray Rectangle"),
    ShapeDefinition(TriangleShape, Color.LightGray, "LightGray Triangle"),
  )

  fun getShape(id: String): ShapeDefinition {
    return shapes.first { it.id == id }
  }
}