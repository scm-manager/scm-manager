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



package sonia.scm.plugin.rest;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginVersion;

//~--- JDK imports ------------------------------------------------------------

import java.util.Comparator;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginInformationComparator
        implements Comparator<PluginInformation>
{

  /** Field description */
  public static final PluginInformationComparator INSTANCE =
    new PluginInformationComparator();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param plugin
   * @param otherPlugin
   *
   * @return
   */
  @Override
  public int compare(PluginInformation plugin, PluginInformation otherPlugin)
  {
    int c = plugin.getGroupId().compareTo(otherPlugin.getGroupId());

    if (c == 0)
    {
      c = plugin.getArtifactId().compareTo(otherPlugin.getArtifactId());

      if (c == 0)
      {
        PluginVersion version =
          PluginVersion.createVersion(plugin.getVersion());
        PluginVersion otherVersion =
          PluginVersion.createVersion(otherPlugin.getVersion());

        c = version.compareTo(otherVersion);
      }
    }

    return c;
  }
}
