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

package sonia.scm.repository.api;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BrowseCommandRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class BrowserResultCollapser {

  private BrowseCommand browseCommand;
  private BrowseCommandRequest request;

  public void collapseFolders(BrowseCommand browseCommand, BrowseCommandRequest request, FileObject fo) throws IOException {
    if (!fo.isDirectory()) {
      return;
    }
    this.browseCommand = browseCommand;
    this.request = request;

    ArrayList<FileObject> collapsedChildren = new ArrayList<>(fo.getChildren());
    ListIterator<FileObject> iter = collapsedChildren.listIterator();
    while (iter.hasNext()) {
      FileObject child = iter.next();
      if (child.isDirectory()) {
        iter.remove();
        traverseFolder(child, iter);
      }
    }
    fo.setChildren(collapsedChildren);
  }

  private void traverseFolder(FileObject fo, ListIterator<FileObject> iter) throws IOException {
    BrowseCommandRequest childRequest = createChildRequest(request, fo.getPath());
    BrowserResult result = browseCommand.getBrowserResult(childRequest);
    if (isEmptyOrHasFiles(result.getFile())) {
      iter.add(fo);
    } else {
      for (FileObject child : result.getFile().getChildren()) {
        child.setName(fo.getName() + "/" + child.getName());
        traverseFolder(child, iter);
      }
    }
  }

  private BrowseCommandRequest createChildRequest(BrowseCommandRequest request, String path) {
    BrowseCommandRequest childRequest = request.clone();
    childRequest.setLimit(Integer.MAX_VALUE);
    childRequest.setOffset(0);
    childRequest.setPath(path);
    return childRequest;
  }

  private boolean isEmptyOrHasFiles(FileObject fo) {
    if (fo.getChildren().isEmpty()) {
      return true;
    }
    for (FileObject child : fo.getChildren()) {
      if (!child.isDirectory()) {
        return true;
      }
    }
    return false;
  }

}
