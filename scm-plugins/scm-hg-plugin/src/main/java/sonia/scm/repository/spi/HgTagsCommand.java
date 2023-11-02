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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import sonia.scm.repository.Tag;
import sonia.scm.util.Util;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Sebastian Sdorra
 */
public class HgTagsCommand extends AbstractCommand implements TagsCommand {

  public static final String DEFAULT_TAG_NAME = "tip";

  /**
   * Constructs ...
   *
   * @param context
   */

  @Inject
  public HgTagsCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @return
   */
  @Override
  public List<Tag> getTags() {
    org.javahg.commands.TagsCommand cmd =
      org.javahg.commands.TagsCommand.on(open());

    List<org.javahg.Tag> tagList = cmd.includeTip().execute();

    List<Tag> tags = null;

    // check for empty repository
    if (Util.isNotEmpty(tagList) && tagList.get(0).getChangeset() != null) {
      tags = Lists.transform(tagList, new TagTransformer());
    }

    if (tags == null) {
      tags = Lists.newArrayList();
    }

    return tags;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   * @author Enter your name here...
   * @version Enter version here..., 12/08/03
   */
  private static class TagTransformer
    implements Function<org.javahg.Tag, Tag> {

    /**
     * Method description
     *
     * @param f
     * @return
     */
    @Override
    public Tag apply(org.javahg.Tag f) {
      Tag t = null;

      if ((f != null) && !Strings.isNullOrEmpty(f.getName())
        && (f.getChangeset() != null)) {
        t = new Tag(f.getName(), f.getChangeset().getNode(), f.getChangeset().getTimestamp().getDate().getTime(), !f.getName().equals(DEFAULT_TAG_NAME));
      }

      return t;
    }
  }

  public interface Factory {
    HgTagsCommand create(HgCommandContext context);
  }

}
