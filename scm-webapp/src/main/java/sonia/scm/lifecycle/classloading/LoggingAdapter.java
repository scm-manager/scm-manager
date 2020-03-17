/**
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
package sonia.scm.lifecycle.classloading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventor;

/**
 * Logging adapter for {@link ClassLoaderLeakPreventor}.
 */
public class LoggingAdapter implements se.jiderhamn.classloader.leak.prevention.Logger {

  @SuppressWarnings("squid:S3416") // suppress "loggers should be named for their enclosing classes" rule
  private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderLeakPreventor.class);

  @Override
  public void debug(String msg) {
    LOG.debug(msg);
  }

  @Override
  public void info(String msg) {
    LOG.info(msg);
  }

  @Override
  public void warn(String msg) {
    LOG.warn(msg);
  }

  @Override
  public void warn(Throwable t) {
    LOG.warn(t.getMessage(), t);
  }

  @Override
  public void error(String msg) {
    LOG.error(msg);
  }

  @Override
  public void error(Throwable t) {
    LOG.error(t.getMessage(), t);
  }
}
