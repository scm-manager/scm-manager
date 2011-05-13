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



package sonia.scm.client.it;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.client.ClientHandler;
import sonia.scm.client.JerseyClientSession;
import sonia.scm.client.it.AbstractClientHandlerTestBase.ModifyTest;
import sonia.scm.group.Group;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyGroupClientHandlerITCase
        extends AbstractClientHandlerTestBase<Group>
{

  /**
   * Method description
   *
   *
   * @param session
   *
   * @return
   */
  @Override
  protected ClientHandler<Group> createHandler(JerseyClientSession session)
  {
    return session.getGroupHandler();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected ModifyTest<Group> createModifyTest()
  {
    return new ModifyTest<Group>()
    {
      @Override
      public void modify(Group item)
      {
        item.setDescription("Modified Description");
      }
      @Override
      public boolean isCorrectModified(Group item)
      {
        return "Modified Description".equals(item.getDescription());
      }
    };
  }

  /**
   * Method description
   *
   *
   * @param number
   *
   * @return
   */
  @Override
  protected Group createTestData(int number)
  {
    return new Group("xml", "group-" + number);
  }
}
