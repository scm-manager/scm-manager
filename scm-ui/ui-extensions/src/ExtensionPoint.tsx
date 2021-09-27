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
import React, { FC, ReactNode } from "react";
import { Binder } from "./binder";
import useBinder from "./useBinder";

type PropTransformer = (props: object) => object;

type Props = {
  name: string;
  renderAll?: boolean;
  props?: object;
  propTransformer?: PropTransformer;
  wrapper?: boolean;
};

const createInstance = (Component: any, props: object, key?: number) => {
  const instanceProps = {
    ...props,
    key,
  };
  if (React.isValidElement(Component)) {
    return React.cloneElement(Component, instanceProps);
  }
  return <Component {...instanceProps} />;
};

const renderAllExtensions = (binder: Binder, name: string, props: object) => {
  const extensions = binder.getExtensions(name, props);
  return <>{extensions.map((cmp, index) => createInstance(cmp, props, index))}</>;
};

const renderWrapperExtensions = (binder: Binder, name: string, props: object) => {
  const extensions = [...(binder.getExtensions(name, props) || [])];
  extensions.reverse();

  let instance: any = null;
  extensions.forEach((cmp, index) => {
    let instanceProps = props;
    if (instance) {
      instanceProps = { ...props, children: instance };
    }
    instance = createInstance(cmp, instanceProps, index);
  });

  return instance;
};

const renderSingleExtension = (binder: Binder, name: string, props: object) => {
  const cmp = binder.getExtension(name, props);
  if (!cmp) {
    return null;
  }
  return createInstance(cmp, props);
};

const renderDefault = (children: ReactNode) => {
  if (children) {
    return <>{children}</>;
  }
  return null;
};

const createRenderProps = (propTransformer?: PropTransformer, props?: object) => {
  const transform = (untransformedProps: object) => {
    if (!propTransformer) {
      return untransformedProps;
    }
    return propTransformer(untransformedProps);
  };

  return transform(props || {});
};

/**
 * ExtensionPoint renders components which are bound to an extension point.
 */
const ExtensionPoint: FC<Props> = ({ name, propTransformer, props, renderAll, wrapper, children }) => {
  const binder = useBinder();
  const renderProps = createRenderProps(propTransformer, { ...(props || {}), children });
  if (!binder.hasExtension(name, renderProps)) {
    return renderDefault(children);
  } else if (renderAll) {
    if (wrapper) {
      return renderWrapperExtensions(binder, name, renderProps);
    }
    return renderAllExtensions(binder, name, renderProps);
  }
  return renderSingleExtension(binder, name, renderProps);
};

export default ExtensionPoint;
