// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.shared.shapes

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.NavDecoration
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import com.slack.circuit.shared.shapes.data.ShapesRepository
import com.slack.circuit.shared.shapes.theme.SharedElementsTheme
import com.slack.circuit.shared.shapes.ui.ShapeDetailsPresenter
import com.slack.circuit.shared.shapes.ui.ShapeDetailsScreen
import com.slack.circuit.shared.shapes.ui.ShapeDetailsUi
import com.slack.circuit.shared.shapes.ui.ShapesPresenter
import com.slack.circuit.shared.shapes.ui.ShapesScreen
import com.slack.circuit.shared.shapes.ui.ShapesUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val circuit: Circuit =
      Circuit.Builder()
        .addPresenterFactory(buildPresenterFactory())
        .addUiFactory(buildUiFactory())
        .build()

    setContent {
      SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
          SharedElementsTheme {
            val backStack = rememberSaveableBackStack(ShapesScreen)
            val navigator = rememberCircuitNavigator(backStack, false)
            CircuitCompositionLocals(circuit) {
              NavigableCircuitContent(
                navigator = navigator,
                backStack = backStack,
                decoration = AnimatedContentDecoration(navigator::pop),
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
              )
            }
          }
        }
      }
    }
  }
}

private fun buildPresenterFactory(): Presenter.Factory =
  Presenter.Factory { screen, navigator, _ ->
    when (screen) {
      is ShapesScreen -> ShapesPresenter(navigator, ShapesRepository)
      is ShapeDetailsScreen -> ShapeDetailsPresenter(screen.id, ShapesRepository)
      else -> null
    }
  }

private fun buildUiFactory(): Ui.Factory =
  Ui.Factory { screen, _ ->
    when (screen) {
      is ShapesScreen -> ui<ShapesScreen.State> { state, modifier -> ShapesUi(state, modifier) }
      is ShapeDetailsScreen -> ui<ShapeDetailsScreen.State> { state, modifier -> ShapeDetailsUi(state, modifier) }
      else -> null
    }
  }

@SuppressLint("ComposeCompositionLocalUsage")
internal val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
  staticCompositionLocalOf { error("SharedTransitionScope not provided") }

private data class AnimatedContentDecoration(
  private val onBackInvoked: () -> Unit,
) : NavDecoration {

  private val forward: ContentTransform by lazy { computeTransition(1) }

  private val backward: ContentTransform by lazy { computeTransition(-1) }

  private fun computeTransition(sign: Int): ContentTransform {
    val enterTransition =
      fadeIn(
        animationSpec =
        tween(
          durationMillis = SHORT_DURATION,
          delayMillis = if (sign > 0) 50 else 0,
          easing = LinearEasing,
        )
      ) +
        slideInHorizontally(
          initialOffsetX = { fullWidth -> (fullWidth / 10) * sign },
          animationSpec =
          tween(durationMillis = NORMAL_DURATION, easing = FastOutExtraSlowInEasing),
        ) +
        if (sign > 0) {
          expandHorizontally(
            animationSpec =
            tween(durationMillis = NORMAL_DURATION, easing = FastOutExtraSlowInEasing),
            initialWidth = { (it * .9f).toInt() },
            expandFrom = if (sign > 0) Alignment.Start else Alignment.End,
          )
        } else {
          EnterTransition.None
        }

    val exitTransition =
      fadeOut(
        animationSpec =
        tween(
          durationMillis = if (sign > 0) NORMAL_DURATION else SHORT_DURATION,
          delayMillis = if (sign > 0) 0 else 50,
          easing = AccelerateEasing,
        )
      ) +
        slideOutHorizontally(
          targetOffsetX = { fullWidth -> (fullWidth / 10) * -sign },
          animationSpec =
          tween(durationMillis = NORMAL_DURATION, easing = FastOutExtraSlowInEasing),
        ) +
        if (sign > 0) {
          shrinkHorizontally(
            animationSpec =
            tween(durationMillis = NORMAL_DURATION, easing = FastOutExtraSlowInEasing),
            targetWidth = { (it * .9f).toInt() },
            shrinkTowards = Alignment.End,
          )
        } else {
          ExitTransition.None
        }

    return enterTransition togetherWith exitTransition
  }

  @Composable
  override fun <T> DecoratedContent(
    args: ImmutableList<T>,
    backStackDepth: Int,
    modifier: Modifier,
    content: @Composable AnimatedContentScope.(T) -> Unit,
  ) {
    var progress by remember { mutableFloatStateOf(0f) }
    var isInPredictiveBack by remember { mutableStateOf(false) }
    PredictiveBackHandler(backStackDepth > 1) { backEvent ->
      progress = 0f
      try {
        backEvent.collect {
          isInPredictiveBack = true
          progress = it.progress
        }
        isInPredictiveBack = false
        onBackInvoked()
      } catch (e: CancellationException) {
        isInPredictiveBack = false
      }
    }
    val transitionState = remember { SeekableTransitionState(args) }
    val transition = rememberTransition(transitionState = transitionState)
    if (isInPredictiveBack) {
      LaunchedEffect(progress) {
        val backstackWithoutTopDestination = args.drop(1)
        transitionState.seekTo(progress, backstackWithoutTopDestination.toPersistentList())
      }
    } else {
      LaunchedEffect(args) {
        if (transitionState.currentState != args) {
          transitionState.animateTo(args)
        }
      }
    }
    transition.AnimatedContent(
      modifier = modifier,
      transitionSpec = {
        // A transitionSpec should only use values passed into the `AnimatedContent`, to
        // minimize
        // the transitionSpec recomposing. The states are available as `targetState` and
        // `initialState`
        val diff = targetState.size - initialState.size
        val sameRoot = targetState.lastOrNull() == initialState.lastOrNull()

        when {
          sameRoot && diff > 0 -> forward
          sameRoot && diff < 0 -> backward
          else -> fadeIn() togetherWith fadeOut()
        }.using(
          // Disable clipping since the faded slide-in/out should
          // be displayed out of bounds.
          SizeTransform(clip = false)
        )
      },
    ) {
      content(it.first())
    }
  }
}

private val FastOutExtraSlowInEasing = CubicBezierEasing(0.208333f, 0.82f, 0.25f, 1f)
private val AccelerateEasing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
private const val DEBUG_MULTIPLIER = 1
private const val SHORT_DURATION = 83 * DEBUG_MULTIPLIER
private const val NORMAL_DURATION = 450 * DEBUG_MULTIPLIER