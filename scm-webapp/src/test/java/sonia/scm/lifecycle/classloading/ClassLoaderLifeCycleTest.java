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
    
package sonia.scm.lifecycle.classloading;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassLoaderLifeCycleTest {

  @Test
  void shouldCreateSimpleClassLoader() {
    System.setProperty(ClassLoaderLifeCycle.PROPERTY, SimpleClassLoaderLifeCycle.NAME);
    try {
      ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();
      assertThat(classLoaderLifeCycle).isInstanceOf(SimpleClassLoaderLifeCycle.class);
    } finally {
      System.clearProperty(ClassLoaderLifeCycle.PROPERTY);
    }
  }

  @Test
  void shouldCreateWithLeakPreventionClassLoader() {
    System.setProperty(ClassLoaderLifeCycle.PROPERTY, ClassLoaderLifeCycleWithLeakPrevention.NAME);
    try {
      ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();
      assertThat(classLoaderLifeCycle).isInstanceOf(ClassLoaderLifeCycleWithLeakPrevention.class);
    } finally {
      System.clearProperty(ClassLoaderLifeCycle.PROPERTY);
    }
  }

  @Test
  void shouldCreateDefaultClassLoader() {
    ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();
    assertThat(classLoaderLifeCycle).isInstanceOf(SimpleClassLoaderLifeCycle.class);
  }
}
