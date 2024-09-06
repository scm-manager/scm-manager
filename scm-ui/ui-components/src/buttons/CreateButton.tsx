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
import styled from "styled-components";
import Button, { ButtonProps } from "./Button";
import classNames from "classnames";

const Wrapper = styled.div`
  border: var(--scm-border);
`;

/**
 * @deprecated Use {@link ui-buttons/src/Button.tsx} instead
 */
export default class CreateButton extends React.Component<ButtonProps> {
  render() {
    return (
      <Wrapper className={classNames("has-text-centered", "mt-5", "p-4")}>
        <Button color="primary" {...this.props} />
      </Wrapper>
    );
  }
}
