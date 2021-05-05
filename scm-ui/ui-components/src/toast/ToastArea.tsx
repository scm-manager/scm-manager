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
import usePortalRootElement from "../usePortalRootElement";
import { createPortal } from "react-dom";
import styled from "styled-components";

const Container = styled.div`
  z-index: 99999;
  position: fixed;
  right: 1.5rem;
  bottom: 1.5rem;

  animation: 0.5s slide-up;

  @keyframes slide-up {
    from {
      bottom: -20rem;
    }
    to {
      bottom: 1.5rem;
    }
  }
`;

const ToastArea: FC = ({ children }) => {
  const rootElement = usePortalRootElement("toastRoot");
  if (!rootElement) {
    // portal not yet ready
    return null;
  }

  return createPortal(<Container>{children}</Container>, rootElement);
};

export default ToastArea;
