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

import java.time.Year

plugins { `dokka-convention` }

subprojects {
  // Coordinates are overridable so non-canonical publishers (e.g. JitPack) can emit self-consistent
  // Gradle metadata. JitPack serves multi-module artifacts under `com.github.<user>.<repo>` and
  // builds a given tag with `VERSION` set; `jitpack.yml` forwards these as `-PvicoGroupId`/
  // `-PvicoVersion`. Locally and for Maven Central, both fall back to the canonical values.
  group = providers.gradleProperty("vicoGroupId").getOrElse("com.patrykandpatrick.vico")
  version = providers.gradleProperty("vicoVersion").getOrElse(Versions.VICO)
}

dependencies {
  dokka(project(":vico:compose"))
  dokka(project(":vico:compose-glance"))
  dokka(project(":vico:compose-m2"))
  dokka(project(":vico:compose-m3"))
}

dokka {
  pluginsConfiguration.html {
    customStyleSheets.from("$rootDir/logo-styles.css")
    footerMessage = "© ${Year.now().value} Patryk Goworowski and Patrick Michalik"
  }
}
