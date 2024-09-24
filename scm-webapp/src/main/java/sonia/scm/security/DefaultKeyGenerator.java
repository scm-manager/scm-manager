/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.security;


import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Base62;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


@Singleton
public class DefaultKeyGenerator implements KeyGenerator
{

  private static final int RANDOM_MAX = 999;

  private static final int RANDOM_MIN = 100;

 
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultKeyGenerator.class);

  private final AtomicLong sessionKey = new AtomicLong();

  private final Random random = new Random();

  public static void main(String[] args)
  {
    System.out.println(new DefaultKeyGenerator().createKey());
  }

  
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

}
