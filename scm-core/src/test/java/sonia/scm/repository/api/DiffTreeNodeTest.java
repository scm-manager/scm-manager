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
