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

package sonia.scm.web;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WireProtocol}.
 */
@RunWith(MockitoJUnitRunner.class)
public class WireProtocolTest {

  @Mock
  private HttpServletRequest request;

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
  public void testGetCommandsOfWithHgArgsPost() throws IOException {
    when(request.getQueryString()).thenReturn("cmd=batch");
    when(request.getIntHeader("X-HgArgs-Post")).thenReturn(29);
    when(request.getHeaderNames()).thenReturn(Collections.enumeration(Lists.newArrayList("X-HgArgs-Post")));
    when(request.getInputStream()).thenReturn(new BufferedServletInputStream("cmds=lheads+%3Bknown+nodes%3D"));

    List<String> commands = WireProtocol.commandsOf(new HgServletRequest(request));
    assertThat(commands, contains("batch", "lheads", "known"));
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
