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

package sonia.scm.user;

/**
 *
 * @author Sebastian Sdorra
 */
public final class UserTestData
{

  /**
   * Constructs ...
   *
   */
  private UserTestData() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static User createAdams()
  {
    return new User("adams", "Douglas Adams", "douglas.adams@hitchhiker.com");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static User createDent()
  {
    return new User("dent", "Arthur Dent", "arthur.dent@hitchhiker.com");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static User createMarvin()
  {
    return new User("marvin", "Marvin", "paranoid.android@hitchhiker.com");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static User createPerfect()
  {
    return new User("perfect", "Ford Prefect", "ford.perfect@hitchhiker.com");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static User createSlarti()
  {
    return new User("slarti", "Slartibartfaß", "slartibartfass@hitchhiker.com");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static User createTrillian()
  {
    User user = new User("trillian", "Tricia McMillan", "tricia.mcmillan@hitchhiker.com");
    user.setPassword("$shiro1$trillisSecret");
    user.setType("xml");
    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static User createZaphod()
  {
    User user = new User("zaphod", "Zaphod Beeblebrox", "zaphod.beeblebrox@hitchhiker.com");
    user.setType("xml");
    user.setPassword("$shiro1$zaphodsSecret");
    return user;
  }
}
