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

package sonia.scm.repository.spi;


import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import sonia.scm.repository.Tag;
import sonia.scm.util.Util;

import java.util.List;


public class HgTagsCommand extends AbstractCommand implements TagsCommand {

  public static final String DEFAULT_TAG_NAME = "tip";



  @Inject
  public HgTagsCommand(@Assisted HgCommandContext context) {
    super(context);
  }



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

  private static class TagTransformer
    implements Function<org.javahg.Tag, Tag> {

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
