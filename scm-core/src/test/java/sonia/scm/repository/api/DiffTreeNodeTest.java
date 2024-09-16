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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffTreeNodeTest {

  @Test
  public void shouldCreateTree() {
    DiffResult.DiffTreeNode root = DiffResult.DiffTreeNode.createRootNode();
    root.addChild("test/a.txt", DiffFile.ChangeType.MODIFY);
    root.addChild("b.txt", DiffFile.ChangeType.DELETE);
    root.addChild("test/c.txt", DiffFile.ChangeType.COPY);
    root.addChild("scm-manager/d.txt", DiffFile.ChangeType.RENAME);
    root.addChild("test/scm-manager/e.txt", DiffFile.ChangeType.ADD);

    assertThat(root.getNodeName()).isEmpty();
    assertThat(root.getChangeType()).isEmpty();
    assertThat(root.getChildren()).containsOnlyKeys("test", "b.txt", "scm-manager");

    assertThat(root.getChildren().get("b.txt").getNodeName()).isEqualTo("b.txt");
    assertThat(root.getChildren().get("b.txt").getChangeType()).hasValue(DiffFile.ChangeType.DELETE);
    assertThat(root.getChildren().get("b.txt").getChildren()).isEmpty();

    assertThat(root.getChildren().get("test").getNodeName()).isEqualTo("test");
    assertThat(root.getChildren().get("test").getChangeType()).isEmpty();
    assertThat(root.getChildren().get("test").getChildren()).containsOnlyKeys("a.txt", "c.txt", "scm-manager");

    assertThat(root.getChildren().get("test").getChildren().get("a.txt").getNodeName()).isEqualTo("a.txt");
    assertThat(root.getChildren().get("test").getChildren().get("a.txt").getChangeType()).hasValue(DiffFile.ChangeType.MODIFY);
    assertThat(root.getChildren().get("test").getChildren().get("a.txt").getChildren()).isEmpty();

    assertThat(root.getChildren().get("test").getChildren().get("c.txt").getNodeName()).isEqualTo("c.txt");
    assertThat(root.getChildren().get("test").getChildren().get("c.txt").getChangeType()).hasValue(DiffFile.ChangeType.COPY);
    assertThat(root.getChildren().get("test").getChildren().get("c.txt").getChildren()).isEmpty();

    assertThat(root.getChildren().get("test").getChildren().get("scm-manager").getNodeName()).isEqualTo("scm-manager");
    assertThat(root.getChildren().get("test").getChildren().get("scm-manager").getChangeType()).isEmpty();
    assertThat(root.getChildren().get("test").getChildren().get("scm-manager").getChildren()).containsOnlyKeys("e.txt");

    assertThat(root.getChildren().get("test").getChildren().get("scm-manager").getChildren().get("e.txt").getNodeName()).isEqualTo("e.txt");
    assertThat(root.getChildren().get("test").getChildren().get("scm-manager").getChildren().get("e.txt").getChangeType()).hasValue(DiffFile.ChangeType.ADD);
    assertThat(root.getChildren().get("test").getChildren().get("scm-manager").getChildren().get("e.txt").getChildren()).isEmpty();

    assertThat(root.getChildren().get("scm-manager").getNodeName()).isEqualTo("scm-manager");
    assertThat(root.getChildren().get("scm-manager").getChangeType()).isEmpty();
    assertThat(root.getChildren().get("scm-manager").getChildren()).containsOnlyKeys("d.txt");

    assertThat(root.getChildren().get("scm-manager").getChildren().get("d.txt").getNodeName()).isEqualTo("d.txt");
    assertThat(root.getChildren().get("scm-manager").getChildren().get("d.txt").getChangeType()).hasValue(DiffFile.ChangeType.RENAME);
    assertThat(root.getChildren().get("scm-manager").getChildren().get("d.txt").getChildren()).isEmpty();
  }
}
