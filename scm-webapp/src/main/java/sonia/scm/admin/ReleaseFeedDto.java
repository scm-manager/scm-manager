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

package sonia.scm.admin;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.xml.XmlUTCDateAdapter;

import java.util.Date;
import java.util.List;

@XmlRootElement(name = "rss")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class ReleaseFeedDto {

  @XmlElement(name = "channel")
  private Channel channel;

  public Channel getChannel() {
    return channel;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class Channel {

    @XmlElement(name = "item")
    private List<Release> releases;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  public static class Release {
    private String title;
    private String description;
    private String link;
    private String guid;
    @XmlJavaTypeAdapter(XmlUTCDateAdapter.class)
    private Date pubDate;
  }
}
