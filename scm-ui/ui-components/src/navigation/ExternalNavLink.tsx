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
import React, { FC, useContext } from "react";
import classNames from "classnames";
import { useSecondaryNavigation } from "@scm-manager/ui-components";
import ExternalLink from "./ExternalLink";
import { SecondaryNavigationContext } from "./SecondaryNavigationContext";

type Props = {
  to: string;
  icon?: string;
  label: string;
};

const ExternalNavLink: FC<Props> = ({ to, icon, label }) => {
  const { collapsed } = useSecondaryNavigation();
  const isSecondaryNavigation = useContext(SecondaryNavigationContext);

  let showIcon;
  if (icon) {
    showIcon = (
      <>
        <i className={classNames(icon, "fa-fw")} />{" "}
      </>
    );
  }

  return (
    <li title={collapsed ? label : undefined}>
      <ExternalLink to={to} className={collapsed ? "has-text-centered" : ""}>
        {showIcon}
        {isSecondaryNavigation && collapsed ? null : label}
      </ExternalLink>
    </li>
  );
};

export default ExternalNavLink;
