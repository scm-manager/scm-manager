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
import * as React from "react";
import { Binder } from "./binder";
import { FC, ReactNode } from "react";
import useBinder from "./useBinder";

type Props = {
  name: string;
  renderAll?: boolean;
  props?: object;
};

const renderAllExtensions = (binder: Binder, name: string, props?: object) => {
  const extensions = binder.getExtensions(name, props);
  return (
    <>
      {extensions.map((Component, index) => {
        return <Component key={index} {...props} />;
      })}
    </>
  );
};

const renderSingleExtension = (binder: Binder, name: string, props?: object) => {
  const Component = binder.getExtension(name, props);
  if (!Component) {
    return null;
  }
  return <Component {...props} />;
};

const renderDefault = (children: ReactNode) => {
  if (children) {
    return <>{children}</>;
  }
  return null;
};

/**
 * ExtensionPoint renders components which are bound to an extension point.
 */
const ExtensionPoint: FC<Props> = ({ name, renderAll, props, children }) => {
  const binder = useBinder();
  if (!binder.hasExtension(name, props)) {
    return renderDefault(children);
  } else if (renderAll) {
    return renderAllExtensions(binder, name, props);
  }
  return renderSingleExtension(binder, name, props);
};

export default ExtensionPoint;
