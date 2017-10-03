/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.internal.download;

/** Listener on {@link Downloader} tasks. */
public interface DownloadProgressListener {

  /**
   * Use this method to display progress: {@code percent downloaded = current * 100 / total}.
   *
   * @param lastChunk bytes read since last update
   * @param completed bytes downloaded so far
   * @param total file size in bytes
   */
  void updateProgress(long lastChunk, long completed, long total);
}