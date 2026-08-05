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

package com.patrykandpatrick.vico.compose.cartesian

import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis

/**
 * Receives single-pointer gestures performed over a [VerticalAxis]. This enables price-scale-style
 * vertical zooming: map drag deltas to changes in the displayed _y_ range (the [CartesianChart]
 * itself doesn’t scale the _y_ range—hosts typically do this via a dynamic
 * `CartesianLayerRangeProvider`).
 *
 * Vertical drags that start over a [VerticalAxis] are consumed and reported here. Horizontal drags
 * and multi-pointer gestures are left to the chart’s scroll and zoom handlers.
 */
public interface VerticalAxisGestureHandler {
  /**
   * Called during a vertical drag over the axis. [deltaY] is the pointer’s vertical displacement
   * in pixels since the previous call (positive when moving down), and [axisHeight] is the height
   * of the axis in pixels, for normalization.
   */
  public fun onDrag(deltaY: Float, axisHeight: Float)

  /** Called when a drag gesture reported via [onDrag] ends or is cancelled. */
  public fun onDragEnd() {}

  /** Called when the axis is double-tapped. */
  public fun onDoubleTap() {}
}
