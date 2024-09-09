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

package sonia.scm.web;


import com.google.common.base.Stopwatch;
import com.google.common.io.Closer;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.GitUtil;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class GitFileHook
{

  public static final String FILE_HOOKDIRECTORY = "hooks";

  public static final String FILE_HOOK_POST_RECEIVE = "post-receive";

  public static final String FILE_HOOK_PRE_RECEIVE = "pre-receive";

 
  private static final Logger logger =
    LoggerFactory.getLogger(GitFileHook.class);

  private final Iterable<ReceiveCommand> commands;

  private final ReceivePack rpack;

  private final RepositoryHookType type;

  private GitFileHook(RepositoryHookType type, ReceivePack rpack,
    Iterable<ReceiveCommand> commands)
  {
    this.type = type;
    this.rpack = rpack;
    this.commands = commands;
  }

  public static void execute(RepositoryHookType type, ReceivePack rpack,
    Iterable<ReceiveCommand> commands)
  {
    new GitFileHook(type, rpack, commands).execute();
  }


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

}
