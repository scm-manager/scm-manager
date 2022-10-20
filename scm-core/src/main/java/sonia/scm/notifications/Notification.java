/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.notifications;

import lombok.Value;

import java.time.Instant;
import java.util.Map;

/**
 * Notifications can be used to send a message to specific user.
 *
 * @since 2.18.0
 */
@Value
public class Notification {

  Type type;
  String link;
  String message;
  Map<String, String> parameters;
  Instant createdAt;

  public Notification(Type type, String link, String message) {
    this(type, link, message, null);
  }

  public Notification(Type type, String link, String message, Map<String, String> parameters) {
    this(type, link, message, parameters, Instant.now());
  }

  Notification(Type type, String link, String message, Map<String, String> parameters, Instant createdAt) {
    this.type = type;
    this.link = link;
    this.message = message;
    this.parameters = parameters;
    this.createdAt = createdAt;
  }

}
