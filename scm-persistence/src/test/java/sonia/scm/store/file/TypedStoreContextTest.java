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
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.TypedStoreParameters;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
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

    context.withMarshaller(marshaller -> {
      marshaller.marshal(new Sample("wow"), file);
    });

    AtomicReference<Sample> ref = new AtomicReference<>();

    context.withUnmarshaller(unmarshaller -> {
      Sample sample = (Sample) unmarshaller.unmarshal(file);
      ref.set(sample);
    });

    assertThat(ref.get().value).isEqualTo("wow");
  }

  @Test
  void shouldSetContextClassLoader() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    ClassLoader classLoader = new URLClassLoader(new URL[0], contextClassLoader);

    TypedStoreParameters<Sample> params = params(Sample.class);
    when(params.getClassLoader()).thenReturn(Optional.of(classLoader));

    TypedStoreContext<Sample> context = TypedStoreContext.of(params);

    AtomicReference<ClassLoader> ref = new AtomicReference<>();
    context.withMarshaller(marshaller -> {
      ref.set(Thread.currentThread().getContextClassLoader());
    });

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

  @SuppressWarnings("unchecked")
  private <T> TypedStoreContext<T> context() {
    return TypedStoreContext.of(params((Class<T>) Sample.class));
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

    public AppendingAdapter(String suffix) {
      this.suffix = suffix;
    }

    @Override
    public String unmarshal(String v) {
      return v + suffix;
    }

    @Override
    public String marshal(String v) {
      return v + suffix;
    }
  }

}
