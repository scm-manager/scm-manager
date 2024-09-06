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

package sonia.scm.repository.api;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BrowseCommandRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class BrowserResultCollapser {

  private BrowseCommand browseCommand;
  private BrowseCommandRequest request;

  public void collapseFolders(BrowseCommand browseCommand, BrowseCommandRequest request, FileObject fo) throws IOException {
    if (!fo.isDirectory()) {
      return;
    }
    this.browseCommand = browseCommand;
    this.request = new BrowseCommandRequest();
    this.request.setRevision(request.getRevision());
    this.request.setDisableLastCommit(true);
    this.request.setLimit(2);

    List<FileObject> collapsedChildren = new ArrayList<>();
    for (FileObject child : fo.getChildren()) {
      if (child.isDirectory() && child.getSubRepository() == null) {
        child = traverseFolder(child);
      }
      collapsedChildren.add(child);
    }
    fo.setChildren(collapsedChildren);
  }

  private FileObject traverseFolder(FileObject parent) throws IOException {
    request.setPath(parent.getPath());
    BrowserResult result = browseCommand.getBrowserResult(request);
    if (isCollapsible(result.getFile())) {
      FileObject child = result.getFile().getChildren().iterator().next();
      child.setName(parent.getName() + "/" + child.getName());
      return traverseFolder(child);
    }
    return parent;
  }

  private boolean isCollapsible(FileObject fo) {
    if (fo.getChildren().size() != 1) {
      return false;
    }
    FileObject child = fo.getChildren().iterator().next();
    return child.isDirectory() && child.getSubRepository() == null;
  }

}
