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
import React, { PropsWithChildren, ReactNode } from "react";
import { Binder, ExtensionPointDefinition } from "./binder";
import useBinder from "./useBinder";

export type Renderable<P> = React.ReactElement | React.ComponentType<P>;
export type RenderableExtensionPointDefinition<Name extends string = string, P = undefined> = ExtensionPointDefinition<
  Name,
  Renderable<P>,
  P
>;

export type SimpleRenderableDynamicExtensionPointDefinition<
  Prefix extends string,
  Suffix extends string | undefined,
  Properties
> = RenderableExtensionPointDefinition<Suffix extends string ? `${Prefix}${Suffix}` : `${Prefix}${string}`, Properties>;

/**
 * @deprecated Obsolete type
 */
type PropTransformer = (props: object) => object;

type BaseProps<E extends RenderableExtensionPointDefinition> = {
  name: E["name"];
  renderAll?: boolean;
  /**
   * @deprecated Obsolete property, do not use
   */
  propTransformer?: PropTransformer;
  wrapper?: boolean;
};

type Props<E extends RenderableExtensionPointDefinition> = BaseProps<E> &
  (E["props"] extends undefined
    ? { props?: E["props"] }
    : {
        props: E["props"];
      });

function createInstance<P>(Component: Renderable<P>, props: P, key?: number) {
  if (React.isValidElement(Component)) {
    return React.cloneElement(Component, {
      ...props,
      ...Component.props,
      key,
    });
  }
  return <Component {...props} key={key} />;
}

const renderAllExtensions = <E extends RenderableExtensionPointDefinition<string, unknown>>(
  binder: Binder,
  name: E["name"],
  props: E["props"]
) => {
  const extensions = binder.getExtensions<E>(name, props);
  return <>{extensions.map((cmp, index) => createInstance(cmp, props, index))}</>;
};

const renderWrapperExtensions = <E extends RenderableExtensionPointDefinition<string, any>>(
  binder: Binder,
  name: E["name"],
  props: E["props"]
) => {
  const extensions = binder.getExtensions<E>(name, props);
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

const renderSingleExtension = <E extends RenderableExtensionPointDefinition<string, unknown>>(
  binder: Binder,
  name: E["name"],
  props: E["props"]
) => {
  const cmp = binder.getExtension<E>(name, props);
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
export default function ExtensionPoint<
  E extends RenderableExtensionPointDefinition<string, any> = RenderableExtensionPointDefinition<string, any>
>({ name, propTransformer, props, renderAll, wrapper, children }: PropsWithChildren<Props<E>>): JSX.Element | null {
  const binder = useBinder();
  const renderProps: E["props"] | {} = createRenderProps(propTransformer, {
    ...(props || {}),
    children,
  });
  if (!binder.hasExtension<E>(name, renderProps)) {
    return renderDefault(children);
  } else if (renderAll) {
    if (wrapper) {
      return renderWrapperExtensions<E>(binder, name, renderProps);
    }
    return renderAllExtensions<E>(binder, name, renderProps);
  }
  return renderSingleExtension<E>(binder, name, renderProps);
}
