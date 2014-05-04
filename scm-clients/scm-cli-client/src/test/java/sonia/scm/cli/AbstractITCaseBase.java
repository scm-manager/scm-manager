/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.cli;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractITCaseBase
{

  /**
   * Method description
   *
   *
   * @param cmd
   *
   * @return
   *
   * @throws IOException
   */
  protected String execute(String... cmd) throws IOException
  {
    String result = null;
    ByteArrayInputStream inputStream = null;
    ByteArrayOutputStream outputStream = null;

    try
    {
      inputStream = new ByteArrayInputStream(new byte[0]);
      outputStream = new ByteArrayOutputStream();

      App app = new App(inputStream, outputStream);

      app.run(cmd);

      outputStream.flush();
      result = outputStream.toString().trim();
    }
    finally
    {
      Closeables.close(inputStream, true);
      Closeables.close(outputStream, true);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param cmd
   *
   * @return
   *
   * @throws IOException
   */
  protected String executeServer(String... cmd) throws IOException
  {
    List<String> cmdList = Lists.newArrayList();

    cmdList.add("--server");
    cmdList.add("http://localhost:8081/scm");
    cmdList.add("--user");
    cmdList.add("scmadmin");
    cmdList.add("--password");
    cmdList.add("scmadmin");
    cmdList.addAll(Arrays.asList(cmd));

    return execute(cmdList.toArray(new String[cmdList.size()]));
  }

  /**
   * Method description
   *
   *
   * @param values
   *
   * @return
   */
  protected String prepareForTest(String values)
  {
    return values.replaceAll("\\n", ";").replaceAll("\\s+", "");
  }
}
