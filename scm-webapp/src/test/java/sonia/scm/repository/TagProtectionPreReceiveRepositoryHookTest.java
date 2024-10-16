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

package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.api.HookFeature;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagProtectionPreReceiveRepositoryHookTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PreReceiveRepositoryHookEvent event;

  @Test
  void shouldDoNothingWithoutGuards() {
    new TagProtectionPreReceiveRepositoryHook(emptySet())
      .onEvent(event);

    verifyNoInteractions(event);
  }

  @Nested
  class WithDeletedTag {

    @BeforeEach
    void setUpEvent() {
      when(event.getRepository()).thenReturn(new Repository("42", "git", "hitchhiker", "hog"));
      when(event.getContext().isFeatureSupported(HookFeature.TAG_PROVIDER)).thenReturn(true);
      when(event.getContext().getTagProvider().getDeletedTags()).thenReturn(List.of(new Tag("protected", "1")));
    }

    @Test
    void shouldProtectTag() {
      TagGuard guard = new TagGuard() {
        @Override
        public boolean canDelete(TagGuardDeletionRequest request) {
          return !(request.getRepository().getId().equals("42")
            && request.getDeletedTag().getName().equals("protected"));
        }
      };

      TagProtectionPreReceiveRepositoryHook hook = new TagProtectionPreReceiveRepositoryHook(Set.of(guard));

      assertThatThrownBy(() -> hook.onEvent(event))
        .isInstanceOf(TagProtectionException.class);
    }

    @Test
    void shouldNotProtectTagIfItCanBeDeleted() {
      TagGuard guard = new TagGuard() {
        @Override
        public boolean canDelete(TagGuardDeletionRequest request) {
          return true;
        }
      };

      TagProtectionPreReceiveRepositoryHook hook = new TagProtectionPreReceiveRepositoryHook(Set.of(guard));

      assertDoesNotThrow(() -> hook.onEvent(event));
    }

  }
}
