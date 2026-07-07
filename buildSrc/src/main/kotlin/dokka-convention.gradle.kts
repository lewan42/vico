/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

plugins { id("org.jetbrains.dokka") }

dokka { dokkaPublications.configureEach { suppressInheritedMembers = true } }

// На JitPack документация не нужна, а его билдер периодически не может докачать зависимости
// Dokka (сбои при создании директорий в его Gradle-кэше) и роняет публикацию. Отключаем задачи
// Dokka — javadoc-jar в такой сборке публикуется пустым. Переменную JITPACK выставляет сам
// билдер JitPack.
if (System.getenv("JITPACK") == "true") {
  tasks.matching { it.name.startsWith("dokka") }.configureEach { enabled = false }
}
