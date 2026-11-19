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

package sonia.scm.store.file;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.xml.XmlGroupDatabase;
import sonia.scm.repository.xml.XmlRepositoryRoleDatabase;
import sonia.scm.store.StoreException;
import sonia.scm.store.TypedStoreParameters;
import sonia.scm.user.xml.XmlUserDatabase;
import sonia.scm.xml.XmlCipherStringAdapter;
import sonia.scm.xml.XmlDatabase;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypedStoreContextTest {

  @Test
  void shouldMarshallAndUnmarshall(@TempDir Path tempDir) {
    TypedStoreContext<Sample> context = context();

    File file = tempDir.resolve("test.xml").toFile();
    context.marshal(new Sample("awesome"), file);
    Sample sample = context.unmarshal(file);

    assertThat(sample.value).isEqualTo("awesome");
  }

  @Test
  void shouldWorkWithMarshallerAndUnmarshaller(@TempDir Path tempDir) {
    TypedStoreContext<Sample> context = context();

    File file = tempDir.resolve("test.xml").toFile();

    context.withMarshaller(
      marshaller -> {
        marshaller.marshal(new Sample("wow"), file);
      },
      file);

    AtomicReference<Sample> ref = new AtomicReference<>();

    context.withUnmarshaller(
      unmarshaller -> {
        Sample sample = (Sample) unmarshaller.unmarshal(file);
        ref.set(sample);
      }, file);

    assertThat(ref.get().value).isEqualTo("wow");
  }

  @Test
  void shouldStoreEncryptedValues(@TempDir Path tempDir) {
    TypedStoreContext<EncryptedSample> context = TypedStoreContext.of(params(EncryptedSample.class));
    EncryptedSample original = new EncryptedSample();
    original.password = "test-password";

    File file = tempDir.resolve("test.xml").toFile();
    context.marshal(original, file);
    EncryptedSample sample = context.unmarshal(file);

    assertThat(sample.password).isEqualTo("test-password");
    assertThat(file).content().doesNotContain("test-password");
  }

  @Test
  void shouldSetContextClassLoader() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    ClassLoader classLoader = new URLClassLoader(new URL[0], contextClassLoader);

    TypedStoreParameters<Sample> params = params(Sample.class);
    when(params.getClassLoader()).thenReturn(Optional.of(classLoader));

    TypedStoreContext<Sample> context = TypedStoreContext.of(params);

    AtomicReference<ClassLoader> ref = new AtomicReference<>();
    context.withMarshaller(
      marshaller -> {
        ref.set(Thread.currentThread().getContextClassLoader());
      },
      "nothing"
    );

    assertThat(ref.get()).isSameAs(classLoader);
    assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(contextClassLoader);
  }

  @Test
  void shouldConfigureAdapter(@TempDir Path tempDir) {
    TypedStoreParameters<SampleWithAdapter> params = params(SampleWithAdapter.class);
    when(params.getAdapters()).thenReturn(Collections.singleton(new AppendingAdapter("!")));

    TypedStoreContext<SampleWithAdapter> context = TypedStoreContext.of(params);

    File file = tempDir.resolve("test.xml").toFile();
    context.marshal(new SampleWithAdapter("awesome"), file);
    SampleWithAdapter sample = context.unmarshal(file);

    // one ! should be added for marshal and one for unmarshal
    assertThat(sample.value).isEqualTo("awesome!!");
  }

  @Test
  void shouldCopyPersistedFields() {
    Sample original = new Sample("original");
    original.transientValue = "derived";

    Sample copy = context().copy(original);
    copy.value = "changed";

    assertThat(original.value).isEqualTo("original");
    assertThat(copy.transientValue).isNull();
  }

  @Test
  void shouldCopyWithConfiguredAdapterAndClassLoader() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader pluginClassLoader = new URLClassLoader(new URL[0], originalClassLoader);
    AppendingAdapter adapter = new AppendingAdapter("!");
    TypedStoreParameters<SampleWithAdapter> params = params(SampleWithAdapter.class);
    when(params.getClassLoader()).thenReturn(Optional.of(pluginClassLoader));
    when(params.getAdapters()).thenReturn(Collections.singleton(adapter));

    SampleWithAdapter copy = TypedStoreContext.of(params).copy(new SampleWithAdapter("original"));

    assertThat(copy.value).isEqualTo("original!!");
    assertThat(adapter.marshalledWith).isSameAs(pluginClassLoader);
    assertThat(adapter.unmarshalledWith).isSameAs(pluginClassLoader);
    assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(originalClassLoader);
  }

  @Test
  void shouldRestoreClassLoaderWhenUnmarshalInCopyFails() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader pluginClassLoader = new URLClassLoader(new URL[0], originalClassLoader);
    AppendingAdapter adapter = new AppendingAdapter("!");
    adapter.failUnmarshal = true;
    adapter.failMarshal = false;
    TypedStoreParameters<SampleWithAdapter> params = params(SampleWithAdapter.class);
    when(params.getClassLoader()).thenReturn(Optional.of(pluginClassLoader));
    when(params.getAdapters()).thenReturn(Collections.singleton(adapter));
    TypedStoreContext<SampleWithAdapter> context = TypedStoreContext.of(params);

    assertThatThrownBy(() -> context.copy(new SampleWithAdapter("original")))
      .isInstanceOf(StoreException.class);

    assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(originalClassLoader);
  }

  @Test
  void shouldRestoreClassLoaderWhenMarshalInCopyFails() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader pluginClassLoader = new URLClassLoader(new URL[0], originalClassLoader);
    AppendingAdapter adapter = new AppendingAdapter("!");
    adapter.failUnmarshal = false;
    adapter.failMarshal = true;
    TypedStoreParameters<SampleWithAdapter> params = params(SampleWithAdapter.class);
    when(params.getClassLoader()).thenReturn(Optional.of(pluginClassLoader));
    when(params.getAdapters()).thenReturn(Collections.singleton(adapter));
    TypedStoreContext<SampleWithAdapter> context = TypedStoreContext.of(params);

    assertThatThrownBy(() -> context.copy(new SampleWithAdapter("original")))
      .isInstanceOf(StoreException.class);

    assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(originalClassLoader);
  }

  @Test
  void shouldCopyEncryptedValues() {
    TypedStoreContext<EncryptedSample> context = TypedStoreContext.of(params(EncryptedSample.class));
    EncryptedSample original = new EncryptedSample();
    original.password = "test-password";

    EncryptedSample copy = context.copy(original);

    assertThat(copy).isNotSameAs(original);
    assertThat(copy.password).isEqualTo(original.password);
  }

  @ParameterizedTest
  @ValueSource(classes = {XmlUserDatabase.class, XmlGroupDatabase.class, XmlRepositoryRoleDatabase.class})
  <T extends XmlDatabase<?>> void shouldCopyEmptyDatabase(Class<T> type) throws ReflectiveOperationException {
    T original = type.getConstructor().newInstance();

    T copy = TypedStoreContext.of(params(type)).copy(original);

    assertThat(copy).isNotSameAs(original);
    assertThat(copy.values()).isEmpty();
  }

  private TypedStoreContext<Sample> context() {
    return TypedStoreContext.of(params(Sample.class));
  }

  @SuppressWarnings("unchecked")
  private <T> TypedStoreParameters<T> params(Class<T> type) {
    TypedStoreParameters<T> params = mock(TypedStoreParameters.class);
    when(params.getType()).thenReturn(type);
    return params;
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Sample {
    private String value;
    @XmlTransient
    private String transientValue;

    public Sample() {
    }

    public Sample(String value) {
      this.value = value;
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SampleWithAdapter {
    @XmlJavaTypeAdapter(AppendingAdapter.class)
    private String value;

    public SampleWithAdapter() {
    }

    public SampleWithAdapter(String value) {
      this.value = value;
    }
  }

  public static class AppendingAdapter extends XmlAdapter<String, String> {

    private final String suffix;
    private ClassLoader marshalledWith;
    private ClassLoader unmarshalledWith;
    private boolean failUnmarshal;
    private boolean failMarshal;

    public AppendingAdapter(String suffix) {
      this.suffix = suffix;
    }

    @Override
    public String unmarshal(String v) {
      unmarshalledWith = Thread.currentThread().getContextClassLoader();
      if (failUnmarshal) {
        throw new IllegalStateException("unmarshal failed");
      }
      return v + suffix;
    }

    @Override
    public String marshal(String v) {
      marshalledWith = Thread.currentThread().getContextClassLoader();
      if (failMarshal) {
        throw new IllegalStateException("marshal failed");
      }
      return v + suffix;
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class EncryptedSample {
    @XmlJavaTypeAdapter(XmlCipherStringAdapter.class)
    private String password;
  }

}
