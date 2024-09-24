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

import * as React from "react";
import classNames from "classnames";
import styled from "styled-components";

const ActionContainer = styled.div`
  @media screen and (max-width: 768px) {
    flex-direction: column;
    > button {
      flex: 1 1 100%;
    }
  }
  @media screen and (min-width: 769px) and (max-width: 1215px) {
    > button {
      flex-grow: 1;
    }
  }
  @media screen and (min-width: 1216px) {
    > button {
      min-width: 25ch;
    }
  }
`;

type Props = {
  children?: React.ReactElement;
};

export default class PluginTopActions extends React.Component<Props> {
  render() {
    const { children } = this.props;
    return (
      <ActionContainer className={classNames("is-flex", "is-align-self-stretch", "has-gap-1")}>
        {children}
      </ActionContainer>
    );
  }
}
