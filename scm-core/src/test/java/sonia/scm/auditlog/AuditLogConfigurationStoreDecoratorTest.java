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

package sonia.scm.auditlog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.StoreDecoratorFactory;
import sonia.scm.store.TypedStoreParameters;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogConfigurationStoreDecoratorTest {

  @Mock
  private Auditor auditor;
  @Mock
  private RepositoryDAO repositoryDAO;
  @Mock
  private ConfigurationStore<Object> delegate;
  @Mock
  private StoreDecoratorFactory.Context storeContext;
  @Mock
  private TypedStoreParameters parameters;

  private AuditLogConfigurationStoreDecorator<Object> decorator;

  @BeforeEach
  void setUpDecorator() {
    decorator = new AuditLogConfigurationStoreDecorator<>(Set.of(auditor), repositoryDAO, delegate, storeContext);
  }

  @Nested
  class WithAuditableEntries {

    @BeforeEach
    void setUpStoreContext() {
      when(storeContext.getStoreParameters()).thenReturn(parameters);
      lenient().when(parameters.getName()).thenReturn("hog");
    }

    @Test
    void shouldCallAuditorForSimpleEntry() {
      Object entry = new SimpleEntry();

      decorator.set(entry);

      verify(auditor).createEntry(argThat(
        context -> {
          assertThat(context.getEntity()).isEmpty();
          assertThat(context.getAdditionalLabels()).contains("hog");
          assertThat(context.getObject()).isSameAs(entry);
          assertThat(context.getOldObject()).isNull();
          return true;
        }
      ));
    }

    @Test
    void shouldCallAuditorForAdditionalLabelEntry() {
      Object entry = new ExtraLabelEntry();

      decorator.set(entry);

      verify(auditor).createEntry(argThat(
        context -> {
          assertThat(context.getEntity()).isEmpty();
          assertThat(context.getAdditionalLabels()).isEmpty();
          assertThat(context.getObject()).isSameAs(entry);
          assertThat(context.getOldObject()).isNull();
          return true;
        }
      ));
    }

    @Test
    void shouldCallDelegateForSimpleEntry() {
      Object entry = new SimpleEntry();

      decorator.set(entry);

      verify(delegate).set(entry);
    }

    @Nested
    class ForRepositoryStore {

      @BeforeEach
      void mockRepositoryContext() {
        when(parameters.getRepositoryId()).thenReturn("42");
        when(repositoryDAO.get("42")).thenReturn(new Repository("42", "git", "hitchhiker", "hog"));
      }

      @Test
      void shouldCallAuditorForSimpleEntry() {
        Object entry = new SimpleEntry();

        decorator.set(entry);

        verify(auditor).createEntry(argThat(
          context -> {
            assertThat(context.getEntity()).isEqualTo("hitchhiker/hog");
            assertThat(context.getAdditionalLabels()).contains("hog");
            assertThat(context.getObject()).isSameAs(entry);
            assertThat(context.getOldObject()).isNull();
            return true;
          }
        ));
      }

      @Test
      void shouldCallAuditorForAdditionalLabelEntry() {
        Object entry = new ExtraLabelEntry();

        decorator.set(entry);

        verify(auditor).createEntry(argThat(
          context -> {
            assertThat(context.getEntity()).isEqualTo("hitchhiker/hog");
            assertThat(context.getAdditionalLabels()).contains("repository");
            assertThat(context.getObject()).isSameAs(entry);
            assertThat(context.getOldObject()).isNull();
            return true;
          }
        ));
      }

      @Test
      void shouldUseOldObjectFromStore() {
        Object oldObject = new Object();
        when(delegate.get()).thenReturn(oldObject);

        Object entry = new SimpleEntry();

        decorator.set(entry);

        verify(auditor).createEntry(argThat(
          context -> {
            assertThat(context.getOldObject()).isSameAs(oldObject);
            return true;
          }
        ));
      }
    }

    @Test
    void shouldUseOldObjectFromStore() {
      Object oldObject = new Object();
      when(delegate.get()).thenReturn(oldObject);

      Object entry = new SimpleEntry();

      decorator.set(entry);

      verify(auditor).createEntry(argThat(
        context -> {
          assertThat(context.getOldObject()).isSameAs(oldObject);
          return true;
        }
      ));
    }
  }

  @Test
  void shouldNotCallAuditorForIgnoredEntry() {
    Object entry = new IgnoredEntry();

    decorator.set(entry);

    verify(auditor, never()).createEntry(any());
  }

  @Test
  void shouldCallDelegateForIgnoredEntry() {
    Object entry = new IgnoredEntry();

    decorator.set(entry);

    verify(delegate).set(entry);
  }

  @AuditEntry
  private static class SimpleEntry {
  }

  @AuditEntry(labels = "permission")
  private static class ExtraLabelEntry {
  }

  @AuditEntry(ignore = true)
  private static class IgnoredEntry {
  }
}

