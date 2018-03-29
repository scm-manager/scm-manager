/**
 * Copyright (c) 2018, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.web;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WireProtocol}.
 */
@RunWith(MockitoJUnitRunner.class)
public class WireProtocolTest {

  @Mock
  private HttpServletRequest request;

  @Test
  public void testIsWriteRequestOnPost() {
    assertIsWriteRequest("capabilities", "unbundle");
  }

  @Test
  public void testIsWriteRequest() {
    assertIsWriteRequest("unbundle");
    assertIsWriteRequest("capabilities", "unbundle");
    assertIsWriteRequest("capabilities", "postkeys");
    assertIsReadRequest();
    assertIsReadRequest("capabilities");
    assertIsReadRequest("capabilities", "branches", "branchmap");
  }

  private void assertIsWriteRequest(String... commands) {
    List<String> cmdList = Lists.newArrayList(commands);
    assertTrue(WireProtocol.isWriteRequest(cmdList));
  }

  private void assertIsReadRequest(String... commands) {
    List<String> cmdList = Lists.newArrayList(commands);
    assertFalse(WireProtocol.isWriteRequest(cmdList));
  }

  @Test
  public void testGetCommandsOf() {
    expectQueryCommand("capabilities", "cmd=capabilities");
    expectQueryCommand("unbundle", "cmd=unbundle");
    expectQueryCommand("unbundle", "prefix=stuff&cmd=unbundle");
    expectQueryCommand("unbundle", "cmd=unbundle&suffix=stuff");
    expectQueryCommand("unbundle", "prefix=stuff&cmd=unbundle&suffix=stuff");
    expectQueryCommand("unbundle", "bool=&cmd=unbundle");
    expectQueryCommand("unbundle", "bool&cmd=unbundle");
    expectQueryCommand("unbundle", "prefix=stu==ff&cmd=unbundle");
  }

  @Test
  public void testGetCommandsOfWithBatch() {
    prepareBatch("cmds=heads ;known nodes,ef5993bb4abb32a0565c347844c6d939fc4f4b98");
    List<String> commands = WireProtocol.commandsOf(request);
    assertThat(commands, contains("batch", "heads", "known"));
  }

  @Test
  public void testGetCommandsOfWithBatchEncoded() {
    prepareBatch("cmds=heads+%3Bknown+nodes%3Def5993bb4abb32a0565c347844c6d939fc4f4b98");
    List<String> commands = WireProtocol.commandsOf(request);
    assertThat(commands, contains("batch", "heads", "known"));
  }

  @Test
  public void testGetCommandsOfWithBatchAndMutlipleLines() {
    prepareBatch(
      "cmds=heads+%3Bknown+nodes%3Def5993bb4abb32a0565c347844c6d939fc4f4b98",
      "cmds=unbundle; postkeys",
      "cmds= branchmap p1=r2,p2=r4; listkeys"
    );
    List<String> commands = WireProtocol.commandsOf(request);
    assertThat(commands, contains("batch", "heads", "known", "unbundle", "postkeys", "branchmap", "listkeys"));
  }

  private void prepareBatch(String... args) {
    when(request.getQueryString()).thenReturn("cmd=batch");
    List<String> headers = Lists.newArrayList();
    for (int i=0; i<args.length; i++) {
      String header = "X-HgArg-" + (i+1);
      headers.add(header);
      when(request.getHeader(header)).thenReturn(args[i]);
    }
    when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetCommandsOfWithMultipleCommandsInQueryString() {
    when(request.getQueryString()).thenReturn("cmd=abc&cmd=def");
    WireProtocol.commandsOf(request);
  }

  @Test
  public void testGetCommandsOfWithoutCmdInQueryString() {
    when(request.getQueryString()).thenReturn("abc=def&123=456");
    assertTrue(WireProtocol.commandsOf(request).isEmpty());
  }

  @Test
  public void testGetCommandsOfWithEmptyQueryString() {
    when(request.getQueryString()).thenReturn("");
    assertTrue(WireProtocol.commandsOf(request).isEmpty());
  }

  @Test
  public void testGetCommandsOfWithNullQueryString() {
    assertTrue(WireProtocol.commandsOf(request).isEmpty());
  }

  private void expectQueryCommand(String expected, String queryString) {
    when(request.getQueryString()).thenReturn(queryString);
    List<String> commands = WireProtocol.commandsOf(request);
    assertEquals(1, commands.size());
    assertTrue(commands.contains(expected));
  }

}
