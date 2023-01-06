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
import React, { FC } from "react";
import styled from "styled-components";
import { Link } from "react-router-dom";
import Tooltip from "../Tooltip";
import Icon from "../Icon";

const Button = styled(Link)`
  width: 50px;
  &:hover {
    color: var(--scm-info-color);
  }
`;

type Props = {
  link: string;
  tooltip: string;
};

const JumpToFileButton: FC<Props> = ({ link, tooltip }) => {
  return (
    <Tooltip message={tooltip} location="bottom">
      <Button aria-label={tooltip} className="button is-clickable" to={link}>
        <Icon name="file-code" color="inherit" alt="" />
      </Button>
    </Tooltip>
  );
};

export default JumpToFileButton;
