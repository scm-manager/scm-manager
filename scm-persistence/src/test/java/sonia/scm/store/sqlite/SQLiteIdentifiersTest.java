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

package sonia.scm.store.sqlite;

import lombok.Getter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.QueryableTypeDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S115") // we do not heed enum naming conventions for better readability in the test
class SQLiteIdentifiersTest {

  @Nested
  class Sanitize {
    @Getter
    private enum BadName {
      OneToOne("examplename or 1=1"),
      BatchedSQLStatement("105; DROP TABLE Classes"),
      CommentOut("--"),
      CommentOutWithContent("spaceship'--"),
      BlindIfInjection("iif(count(*)>2,\"True\",\"False\")"),
      VersionRequest("splite_version()"),
      InnocentNameWithSpace("Traumschiff Enterprise");

      BadName(String name) {
        this.name = name;
      }

      private final String name;
    }

    @Getter
    private enum GoodName {
      Alphabetical("spaceship"),
      AlphabeticalWithUnderscore("spaceship_STORE"),
      Alphanumerical("rollerCoaster2000"),
      AlphanumericalWithUnderscore("rollerCoaster2000_STORE");

      GoodName(String name) {
        this.name = name;
      }

      private final String name;
    }

    @ParameterizedTest
    @EnumSource(BadName.class)
    void shouldBlockSuspiciousNames(BadName name) {
      assertThatThrownBy(() -> SQLiteIdentifiers.sanitize(name.getName()));
    }

    @ParameterizedTest
    @EnumSource(GoodName.class)
    void shouldPassCorrectNames(GoodName name) {
      String outputName = SQLiteIdentifiers.sanitize(name.getName());
      assertThat(outputName).isEqualTo(name.getName());
    }
  }

  @Nested
  class ComputeTableName {
    @Mock
    QueryableTypeDescriptor typeDescriptor;

    void setUp(String clazzName, String name) {
      lenient().when(typeDescriptor.getClazz()).thenReturn(clazzName);
      lenient().when(typeDescriptor.getName()).thenReturn(name);
    }

    @Test
    void shouldReturnCorrectTableNameIncludingPath() {
      setUp("sonia.scm.store.sqlite.Spaceship", null);

      String output = SQLiteIdentifiers.computeTableName(typeDescriptor);

      assertThat(output).isEqualTo("sonia_scm_store_sqlite_Spaceship_STORE");
    }

    @Test
    void shouldReturnTableNameEscapingUnderscores() {
      setUp("sonia.scm.store.sqlite.Spaceship_One", null);

      String output = SQLiteIdentifiers.computeTableName(typeDescriptor);

      assertThat(output).isEqualTo("sonia_scm_store_sqlite_Spaceship__One_STORE");
    }

    @Test
    void shouldReturnCorrectNameWithName() {
      setUp("sonia.scm.store.sqlite.Spaceship", "TraumschiffEnterprise");

      String output = SQLiteIdentifiers.computeTableName(typeDescriptor);

      assertThat(output).isEqualTo("TraumschiffEnterprise_STORE");
    }
  }

  @Nested
  class ComputeColumnIdentifier {
    @Test
    void shouldReturnIdOnlyWithNullValue() {
      assertThat(SQLiteIdentifiers.computeColumnIdentifier(null)).isEqualTo("ID");
    }

    @Test
    void shouldReturnCombinedNameWithGivenClassName() {
      assertThat(SQLiteIdentifiers.computeColumnIdentifier("sonia.scm.store.sqlite.Spaceship.class")).isEqualTo("Spaceship_ID");
    }
  }
}
