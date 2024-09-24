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
