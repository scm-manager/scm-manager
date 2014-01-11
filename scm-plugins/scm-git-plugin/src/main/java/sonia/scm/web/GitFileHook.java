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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Stopwatch;
import com.google.common.io.Closer;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.GitUtil;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitFileHook
{

  /** Field description */
  public static final String FILE_HOOKDIRECTORY = "hooks";

  /** Field description */
  public static final String FILE_HOOK_POST_RECEIVE = "post-receive";

  /** Field description */
  public static final String FILE_HOOK_PRE_RECEIVE = "pre-receive";

  /**
   * the logger for GitFileHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitFileHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   *
   * @param executor
   * @param type
   * @param rpack
   * @param commands
   */
  private GitFileHook(RepositoryHookType type, ReceivePack rpack,
    Iterable<ReceiveCommand> commands)
  {
    this.type = type;
    this.rpack = rpack;
    this.commands = commands;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param type
   * @param rpack
   * @param commands
   */
  public static void execute(RepositoryHookType type, ReceivePack rpack,
    Iterable<ReceiveCommand> commands)
  {
    new GitFileHook(type, rpack, commands).execute();
  }

  /**
   * Method description
   *
   *
   * @param hook
   *
   * @return
   *
   * @throws IOException
   */
  private Process createProcess(File hook) throws IOException
  {
    ProcessBuilder pb = new ProcessBuilder(hook.getAbsolutePath());

    // use repostitory directory as working directory for file hooks
    // see issue #99
    pb.directory(rpack.getRepository().getDirectory());

    // copy system environment for hook
    pb.environment().putAll(System.getenv());

    // start process
    return pb.redirectErrorStream(true).start();
  }

  /**
   * Method description
   *
   *
   * @param rc
   *
   * @return
   */
  private String createReceiveCommandOutput(ReceiveCommand rc)
  {
    StringBuilder sb = new StringBuilder();

    sb.append(GitUtil.getId(rc.getOldId()));
    sb.append(" ");
    sb.append(GitUtil.getId(rc.getNewId()));
    sb.append(" ");
    sb.append(rc.getRefName());

    return sb.toString();
  }

  /**
   * Method description
   *
   *
   * @param type
   */
  private void execute()
  {
    File hook = getHookFile();

    if ((hook == null) ||!hook.exists())
    {
      logger.trace("no file hook found for {}", type);
    }
    else if (!hook.canExecute())
    {
      logger.warn("hook file {} is not executeable", hook);
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("try to execute hook {}", hook);

        Stopwatch sw = Stopwatch.createStarted();

        execute(hook);
        logger.debug("file hook {} executed in {}", hook, sw.stop());
      }
      else
      {
        execute(hook);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param hook
   */
  private void execute(File hook)
  {
    Process p;
    Closer closer = Closer.create();

    try
    {
      p = createProcess(hook);

      PrintWriter writer =
        closer.register(new PrintWriter(p.getOutputStream()));
      BufferedReader stdReader = closer.register(
                                   new BufferedReader(
                                     new InputStreamReader(
                                       p.getInputStream())));

      for (ReceiveCommand rc : commands)
      {
        String output = createReceiveCommandOutput(rc);

        logger.trace("write rc output \"{}\" to hook {}", output, hook);
        writer.println(output);
      }

      writer.close();

      String line = stdReader.readLine();

      while (line != null)
      {
        rpack.sendMessage(line);
        line = stdReader.readLine();
      }

      // TODO handle timeout
      int result = p.waitFor();

      if (result == 0)
      {
        logger.debug("file hook {} executed successful", hook);
      }
      else
      {
        logger.warn("file hook {} returned with exit code {}", hook, result);
        GitHooks.abortIfPossible(type, rpack, commands);
      }
    }
    catch (Exception ex)
    {
      logger.error("failure during file hook execution", ex);
      GitHooks.abortIfPossible(type, rpack, commands,
        "failure during file hook execution");
    }
    finally
    {
      IOUtil.close(closer);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  private File getHookFile()
  {
    File hook = null;
    File directory = rpack.getRepository().getDirectory();
    String scriptName = null;

    if (type == RepositoryHookType.POST_RECEIVE)
    {
      scriptName = FILE_HOOK_POST_RECEIVE;
    }
    else if (type == RepositoryHookType.PRE_RECEIVE)
    {
      scriptName = FILE_HOOK_PRE_RECEIVE;
    }

    if (scriptName != null)
    {
      hook = getHookFile(directory, scriptName);
    }

    return hook;
  }

  /**
   * Method description
   *
   *
   * @param directory
   * @param name
   *
   * @return
   */
  private File getHookFile(File directory, String name)
  {
    //J-
    return IOUtil.getScript(
      new File(
        directory,
        FILE_HOOKDIRECTORY.concat(File.separator).concat(name)
      )
    );
    //J+
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Iterable<ReceiveCommand> commands;

  /** Field description */
  private final ReceivePack rpack;

  /** Field description */
  private final RepositoryHookType type;
}
