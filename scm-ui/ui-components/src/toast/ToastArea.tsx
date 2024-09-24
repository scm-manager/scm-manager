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
