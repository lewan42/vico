/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.compose.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import com.patrykandpatrick.vico.compose.cartesian.VerticalAxisGestureHandler
import kotlin.math.abs

private enum class AxisGesture {
  Tap,
  Drag,
  Cancelled,
}

private class AxisPointerResult(val gesture: AxisGesture, val up: PointerInputChange? = null)

/**
 * Detects single-pointer gestures that start over one of [getAxisBounds]: vertical drags are
 * consumed and reported to [handler] as they happen; two consecutive taps are reported as a double
 * tap. Gestures that start outside the bounds, turn horizontal, or gain a second pointer are left
 * unconsumed for the chart’s scroll, marker, and zoom handlers.
 */
internal suspend fun PointerInputScope.detectVerticalAxisGestures(
  getAxisBounds: () -> List<Rect>,
  handler: VerticalAxisGestureHandler,
) {
  awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)
    val bounds =
      getAxisBounds().firstOrNull { !it.isEmpty && it.contains(down.position) }
        ?: return@awaitEachGesture
    val first = trackAxisPointer(down.id, bounds.height, handler)
    if (first.gesture != AxisGesture.Tap) return@awaitEachGesture
    val secondDown = awaitSecondDown(first.up ?: down) ?: return@awaitEachGesture
    if (!bounds.contains(secondDown.position)) return@awaitEachGesture
    // The second pointer can either tap (double tap) or immediately drag; a drag is reported via
    // `handler.onDrag` by `trackAxisPointer` itself.
    val second = trackAxisPointer(secondDown.id, bounds.height, handler)
    if (second.gesture == AxisGesture.Tap) {
      second.up?.consume()
      handler.onDoubleTap()
    }
  }
}

/**
 * Follows the pointer with [pointerId] until it’s lifted, consumed elsewhere, joined by a second
 * pointer, or moves past the touch slop. Once a vertical drag is recognized, its deltas are
 * consumed and forwarded to [handler]; a horizontal move hands the gesture back to the chart.
 */
private suspend fun AwaitPointerEventScope.trackAxisPointer(
  pointerId: PointerId,
  axisHeight: Float,
  handler: VerticalAxisGestureHandler,
): AxisPointerResult {
  val touchSlop = viewConfiguration.touchSlop
  val longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis
  val downTimeMillis = currentEvent.changes.firstOrNull { it.id == pointerId }?.uptimeMillis ?: 0L
  var dragging = false
  var slopDx = 0f
  var slopDy = 0f
  while (true) {
    val event = awaitPointerEvent()
    val change = event.changes.firstOrNull { it.id == pointerId }
    if (change == null || change.isConsumed || event.changes.count { it.pressed } > 1) {
      if (dragging) handler.onDragEnd()
      return AxisPointerResult(AxisGesture.Cancelled)
    }
    if (!change.pressed) {
      if (dragging) {
        handler.onDragEnd()
        return AxisPointerResult(AxisGesture.Drag)
      }
      val isTap = change.uptimeMillis - downTimeMillis < longPressTimeoutMillis
      return AxisPointerResult(if (isTap) AxisGesture.Tap else AxisGesture.Cancelled, change)
    }
    val delta = change.position - change.previousPosition
    if (!dragging) {
      slopDx += delta.x
      slopDy += delta.y
      when {
        abs(slopDy) > touchSlop && abs(slopDy) > abs(slopDx) -> {
          dragging = true
          handler.onDrag(slopDy, axisHeight)
          change.consume()
        }
        abs(slopDx) > touchSlop -> return AxisPointerResult(AxisGesture.Cancelled)
      }
    } else {
      if (delta.y != 0f) handler.onDrag(delta.y, axisHeight)
      change.consume()
    }
  }
}

/**
 * Awaits a second down event within the double-tap timeout, ignoring events that follow [firstUp]
 * too closely to be a deliberate second tap (mirrors `androidx.compose.foundation`’s internal
 * `awaitSecondDown`).
 */
private suspend fun AwaitPointerEventScope.awaitSecondDown(
  firstUp: PointerInputChange
): PointerInputChange? =
  withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
    val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
    var change: PointerInputChange
    do {
      change = awaitFirstDown()
    } while (change.uptimeMillis < minUptime)
    change
  }
