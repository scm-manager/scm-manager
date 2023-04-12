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
    
package sonia.scm.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AnonymousMode;
import sonia.scm.user.UserManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScmConfigurationChangedListenerTest {

  @Mock
  private UserManager userManager;

  private final ScmConfiguration scmConfiguration = new ScmConfiguration();

  private ScmConfigurationChangedListener listener;

  @BeforeEach
  void initListener() {
    listener = new ScmConfigurationChangedListener(userManager);
  }

  @Test
  void shouldCreateAnonymousUserIfAnonymousAccessEnabled() {
    when(userManager.contains(any())).thenReturn(false);

    ScmConfiguration changes = new ScmConfiguration();
    changes.setAnonymousMode(AnonymousMode.FULL);
    scmConfiguration.load(changes);

    listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager).create(any());
  }

  @Test
  void shouldNotCreateAnonymousUserIfAlreadyExists() {
    when(userManager.contains(any())).thenReturn(true);

    ScmConfiguration changes = new ScmConfiguration();
    changes.setAnonymousMode(AnonymousMode.FULL);
    scmConfiguration.load(changes);

    listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager, never()).create(any());
  }

  @Test
  void shouldNotCreateAnonymousUserIfAnonymousAccessDisabled() {
    //anonymous access disabled by default
     listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager, never()).create(any());
  }
}
