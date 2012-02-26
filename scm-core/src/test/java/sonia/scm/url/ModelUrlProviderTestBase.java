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


package sonia.scm.url;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class ModelUrlProviderTestBase extends UrlTestBase
{

  /** Field description */
  public static final String ITEM = "hitchhiker";

  /** Field description */
  public static final String MODEL_GROUPS = "groups";

  /** Field description */
  public static final String MODEL_REPOSITORY = "repositories";

  /** Field description */
  public static final String MODEL_USERS = "users";

  /** Field description */
  private static final String[] MODELS = new String[] { MODEL_REPOSITORY,
          MODEL_USERS, MODEL_GROUPS };

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @return
   */
  protected abstract ModelUrlProvider createGroupModelUrlProvider(
          String baseUrl);

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @return
   */
  protected abstract ModelUrlProvider createRepositoryModelUrlProvider(
          String baseUrl);

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @return
   */
  protected abstract ModelUrlProvider createUserModelUrlProvider(
          String baseUrl);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param model
   *
   * @return
   */
  protected abstract String getExpectedAllUrl(String baseUrl, String model);

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param model
   * @param item
   *
   * @return
   */
  protected abstract String getExpectedDetailUrl(String baseUrl, String model,
          String item);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void testGetAllUrl()
  {
    for (String model : MODELS)
    {
      assertEquals(getExpectedAllUrl(BASEURL, model),
                   createModelUrlProvider(BASEURL, model).getAllUrl());
    }
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetDetailUrl()
  {
    for (String model : MODELS)
    {
      assertEquals(getExpectedDetailUrl(BASEURL, model, ITEM),
                   createModelUrlProvider(BASEURL, model).getDetailUrl(ITEM));
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @param model
   *
   * @return
   */
  private ModelUrlProvider createModelUrlProvider(String baseUrl, String model)
  {
    ModelUrlProvider urlProvider = null;

    if (MODEL_REPOSITORY.equals(model))
    {
      urlProvider = createRepositoryModelUrlProvider(baseUrl);
    }
    else if (MODEL_USERS.equals(model))
    {
      urlProvider = createUserModelUrlProvider(baseUrl);
    }
    else if (MODEL_GROUPS.equals(model))
    {
      urlProvider = createGroupModelUrlProvider(baseUrl);
    }

    return urlProvider;
  }
}
