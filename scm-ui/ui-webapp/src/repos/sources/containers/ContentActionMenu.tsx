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

import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import React, { FC, useState } from "react";
import { ContentActionExtensionProps } from "./Content";
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";

const Menu = styled.div`
  position: absolute;
  border-radius: 2px;
  z-index: 99;
  border: solid 1px whitesmoke; //TODO Replace white global color definition
  ul {
    border-bottom: red solid 1px;
  }

  li + li:not(last-child) {
    border-bottom: solid 1px red;
  }
`;

const Category = styled.span`
  font-weight: bold;
  padding: 0.25rem;
`;

type Props = {
  extensionProps: ContentActionExtensionProps;
};

const ContentActionMenu: FC<Props> = ({ extensionProps }) => {
  const [showMenu, setShowMenu] = useState(false);
  const extensions = binder.getExtensions<extensionPoints.FileViewActionBarOverflowMenu>(
    "repos.sources.content.actionbar.menu",
    extensionProps
  );

  if (extensions.length <= 0) {
    return null;
  }

  if (extensions.length === 1) {
    const extension = extensions[0];
    return <Button title={extension.label} action={() => extension.action} icon={extension.icon} />;
  }

  const renderMenuTrigger = () => {
    if (showMenu) {
      return <Icon className="has-cursor-pointer" name="times" onClick={() => setShowMenu(false)} color="inherit" />;
    }
    return <Icon className="has-cursor-pointer" name="ellipsis-v" onClick={() => setShowMenu(true)} color="inherit" />;
  };

  // eslint-disable-next-line @typescript-eslint/ban-ts-ignore
  // @ts-ignore This seems to be the best way to remove duplicate array entries
  // https://stackoverflow.com/questions/1960473/get-all-unique-values-in-a-javascript-array-remove-duplicates
  const categories = extensions.map(e => e.category);

  return (
    <div>
      {renderMenuTrigger()}
      {showMenu ? (
        <Menu className="has-background-white p-2">
          {categories.map(c => {
            return (
              <ul>
                <Category>{c}</Category>
                {extensions
                  .filter(e => e.category === c)
                  .map(e => (
                    <li className="has-cursor-pointer p-1" onClick={() => e.action}>
                      <Icon name={e.icon} color="inherit" />
                      {e.label}
                    </li>
                  ))}
              </ul>
            );
          })}
        </Menu>
      ) : null}
    </div>
  );
};

export default ContentActionMenu;
