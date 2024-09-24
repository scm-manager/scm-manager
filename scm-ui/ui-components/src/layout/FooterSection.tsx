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

import React, { FC, ReactNode } from "react";
import classNames from "classnames";
import styled from "styled-components";

type Props = {
  title: ReactNode;
};

const Menu = styled.ul`
  padding-left: 1.1rem;
`;

const FooterSection: FC<Props> = ({ title, children }) => {
  return (
    <section className={classNames("column", "is-one-third")}>
      <h2 className={classNames("has-text-weight-bold", "mb-2")}>{title}</h2>
      <Menu>{children}</Menu>
    </section>
  );
};

export default FooterSection;
