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

import com.google.inject.Inject;

import com.thebuzzmedia.imgscalr.Scalr;

import sonia.scm.plugin.PluginBackend;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginUtil;
import sonia.scm.util.ChecksumUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.List;

import javax.imageio.ImageIO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("/screenshot/{groupId}/{artifactId}/{number}/{size}.jpg")
public class ScreenshotResource
{

  /** Field description */
  public static final String EXTENSION_IMAGE = ".jpg";

  /** Field description */
  public static final String FORMAT = "jpg";

  /** Field description */
  public static final String PATH_IMAGE = "images";

  /** Field description */
  public static final int SIZE_LARGE = 640;

  /** Field description */
  public static final int SIZE_MEDIUM = 320;

  /** Field description */
  public static final int SIZE_SMALL = 200;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param backend
   */
  @Inject
  public ScreenshotResource(PluginBackend backend)
  {
    this.backend = backend;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groupId
   * @param artifactId
   * @param number
   * @param size
   *
   * @return
   *
   * @throws IOException
   */
  @GET
  @Produces("image/jpeg")
  public Response getScreenshot(@PathParam("groupId") String groupId,
                                @PathParam("artifactId") String artifactId,
                                @PathParam("number") int number,
                                @PathParam("size") String size)
          throws IOException
  {
    PluginInformation plugin = PluginUtil.getLatestPluginVersion(backend,
                                 groupId, artifactId);

    if (plugin == null)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    List<String> screenshots = plugin.getScreenshots();

    if (Util.isEmpty(screenshots))
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    String screenshot = screenshots.get(number);

    if (Util.isEmpty(screenshot))
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    String checksum = ChecksumUtil.createChecksum(screenshot);
    StringBuilder path = new StringBuilder(PATH_IMAGE);

    path.append(File.separator).append(groupId);
    path.append(File.separator).append(artifactId).append(File.separator);
    path.append(String.valueOf(number)).append(File.separator);
    path.append(size).append(File.separator).append(checksum);
    path.append(EXTENSION_IMAGE);

    File file = new File(backend.getBaseDirectory(), path.toString());

    if (!file.exists())
    {
      createThumbnail(file, screenshot, size.toCharArray()[0]);
    }

    return Response.ok(file).build();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   * @param screenshot
   * @param size
   *
   * @throws IOException
   */
  private void createThumbnail(File file, String screenshot, char size)
          throws IOException
  {
    IOUtil.mkdirs(file.getParentFile());

    int width = SIZE_SMALL;

    switch (size)
    {
      case 'l' :
        width = SIZE_LARGE;

        break;

      case 'm' :
        width = SIZE_MEDIUM;
    }

    BufferedImage image = ImageIO.read(new URL(screenshot));

    image = Scalr.resize(image, Scalr.Method.QUALITY, width);
    ImageIO.write(image, FORMAT, file);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PluginBackend backend;
}
