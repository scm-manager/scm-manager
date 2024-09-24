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

import React from "react";
import { urls } from "@scm-manager/ui-api";

type Props = {
  src: string;
  alt: string;
  title?: string;
  className?: string;
};

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */
class Image extends React.Component<Props> {
  createImageSrc = () => {
    const { src } = this.props;
    if (src.startsWith("http")) {
      return src;
    }
    return urls.withContextPath(src);
  };

  render() {
    const { alt, title, className } = this.props;
    return <img className={className} src={this.createImageSrc()} alt={alt} title={title} />;
  }
}

export default Image;
