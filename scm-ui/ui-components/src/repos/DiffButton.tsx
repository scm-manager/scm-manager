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
import React, { FC, MouseEvent } from "react";
import styled from "styled-components";
import { Tooltip } from "@scm-manager/ui-overlays";

const Button = styled.button`
  width: 50px;
  &:hover {
    color: var(--scm-info-color);
  }
`;

type Props = {
  icon: string;
  tooltip: string;
  onClick: () => void;
};

const DiffButton: FC<Props> = ({ icon, tooltip, onClick }) => {
  const handleClick = (e: MouseEvent) => {
    e.preventDefault();
    onClick();
  };

  return (
    <Tooltip message={tooltip} side="top">
      <Button aria-label={tooltip} className="button is-clickable" onClick={handleClick}>
        <i className={`fas fa-${icon}`} />
      </Button>
    </Tooltip>
  );
};

export default DiffButton;
