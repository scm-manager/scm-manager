/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class HashBuilderTestBase
{

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract HashBuilder createHashBuilder();

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getLable();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void testCreateIteratedSaltedHash()
  {
    HashBuilder hashBuilder = createHashBuilder();
    String hash = hashBuilder.setIterations(1000).createSalt().setValue(
                      "hitcheker").toHexString();
    byte[] salt = hashBuilder.getSalt();

    hashBuilder = createHashBuilder();

    String otherHash = hashBuilder.setIterations(1000).setSalt(salt).setValue(
                           "hitcheker").toHexString();

    checkHash("hitcheker", hash, otherHash);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateLabledHash()
  {
    HashBuilder hashBuilder = createHashBuilder();
    String hash = hashBuilder.enableLabel().setValue("hitcheker").toHexString();

    System.out.println(hash);
    checkHash("hitcheker", hash);

    Pattern p = Pattern.compile("\\{([^\\}]+)\\}.*");
    Matcher m = p.matcher(hash);

    assertTrue(m.matches());

    String lable = m.group(1);

    assertNotNull(lable);
    assertEquals(getLable(), lable);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateSalt()
  {
    HashBuilder hashBuilder = createHashBuilder();

    assertNotNull(hashBuilder);

    byte[] salt = hashBuilder.createSalt().getSalt();

    assertNotNull(salt);

    byte[] otherSalt = hashBuilder.createSalt().getSalt();

    assertNotNull(otherSalt);
    assertEquals(salt.length, otherSalt.length);
    assertThat(salt, not(equalTo(otherSalt)));
    salt = hashBuilder.createSalt(4).getSalt();
    assertEquals(4, salt.length);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateSaltedHash()
  {
    HashBuilder hashBuilder = createHashBuilder();
    String hash = hashBuilder.createSalt().setValue("hitcheker").toHexString();
    byte[] salt = hashBuilder.getSalt();

    hashBuilder = createHashBuilder();

    String otherHash =
      hashBuilder.setSalt(salt).setValue("hitcheker").toHexString();

    checkHash("hitcheker", hash, otherHash);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateSimpleHash()
  {
    HashBuilder hashBuilder = createHashBuilder();
    String hash = hashBuilder.setValue("hitcheker").toHexString();

    hashBuilder = createHashBuilder();

    String otherHash = hashBuilder.setValue("hitcheker").toHexString();

    checkHash("hitcheker", hash, otherHash);
  }

  /**
   * Method description
   *
   *
   * @param plain
   * @param hash
   */
  private void checkHash(String plain, String hash)
  {
    assertNotNull(hash);
    assertThat(hash, not(equalTo(plain)));
  }

  /**
   * Method description
   *
   *
   * @param plain
   * @param hash
   * @param otherHash
   */
  private void checkHash(String plain, String hash, String otherHash)
  {
    checkHash(plain, hash);
    assertNotNull(otherHash);
    assertThat(otherHash, not(equalTo("hitcheker")));
    assertEquals(hash, otherHash);
  }
}
