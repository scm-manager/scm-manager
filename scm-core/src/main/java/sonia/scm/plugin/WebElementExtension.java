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

package sonia.scm.plugin;

import lombok.Value;

/**
 * WebElementExtension can be a servlet or filter which is ready to bind.
 * Those extensions are loaded by the {@link ExtensionProcessor} from a {@link WebElementDescriptor}.
 *
 * We don't know if we can load the defined class from the descriptor, because the class could be optional
 * (annotated with {@link Requires}). So we have to load the class as string with {@link WebElementDescriptor} and when
 * we know that it is safe to load the class (all requirements are fulfilled), we will create our WebElementExtension.
 *
 * @since 2.0.0
 */
@Value
public class WebElementExtension {
  Class<?> clazz;
  WebElementDescriptor descriptor;
}
