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

package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import sonia.scm.repository.api.UsernamePasswordCredential;

final class GitMirrorTransportConfigurer {

  private final MirrorHttpConnectionProvider connectionProvider;
  private final MirrorCommandRequest request;
  private final GitMirrorCommand.MirrorLogWithCallback log;

  GitMirrorTransportConfigurer(MirrorHttpConnectionProvider connectionProvider, MirrorCommandRequest request,
                               GitMirrorCommand.MirrorLogWithCallback log) {
    this.connectionProvider = connectionProvider;
    this.request = request;
    this.log = log;
  }

  void configure(TransportCommand<?, ?> command) {
    command.setTransportConfigCallback(transport -> {
      if (transport instanceof TransportHttp transportHttp) {
        transportHttp.setHttpConnectionFactory(connectionProvider.createHttpConnectionFactory(request, log));
      }
    });
    request.getCredential(UsernamePasswordCredential.class)
      .ifPresent(credential -> command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
        Strings.nullToEmpty(credential.username()),
        Strings.nullToEmpty(new String(credential.password()))
      )));
  }
}
