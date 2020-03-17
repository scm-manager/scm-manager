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


package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Base62;

//~--- JDK imports ------------------------------------------------------------

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultKeyGenerator implements KeyGenerator
{

  /** Field description */
  private static final int RANDOM_MAX = 999;

  /** Field description */
  private static final int RANDOM_MIN = 100;

  /**
   * the logger for DefaultKeyGenerator
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultKeyGenerator.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   */
  public static void main(String[] args)
  {
    System.out.println(new DefaultKeyGenerator().createKey());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String createKey()
  {
    StringBuilder buffer = new StringBuilder();

    buffer.append(Base62.encode(createRandom()));
    buffer.append(Base62.encode(System.currentTimeMillis()));
    buffer.append(Base62.encode(sessionKey.incrementAndGet()));

    String key = buffer.toString();

    if (logger.isTraceEnabled())
    {
      logger.trace("create new key {}", key);
    }

    return key;
  }

  /**
   * Create a random int between {@link #RANDOM_MIN} and {@link #RANDOM_MAX}.
   * This method is package visible for testing.
   *
   * @return a random int between the min and max value
   */
  int createRandom()
  {
    return random.nextInt(RANDOM_MAX - RANDOM_MIN + 1) + RANDOM_MIN;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final AtomicLong sessionKey = new AtomicLong();

  /** Field description */
  private final Random random = new Random();
}
