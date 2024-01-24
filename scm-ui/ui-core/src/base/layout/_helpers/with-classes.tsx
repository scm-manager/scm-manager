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

import React, { ComponentProps, ElementRef, ForwardRefExoticComponent, ReactElement } from "react";
import classNames from "classnames";
import { Slot } from "@radix-ui/react-slot";

const withClasses = <Element extends React.ElementType | ForwardRefExoticComponent<any>>(
  typ: Element,
  additionalClassNames: string[],
  additionalProps?: Partial<ComponentProps<Element>>
) =>
  React.forwardRef<
    ElementRef<Element>,
    | (ComponentProps<Element> & { asChild?: false; className?: string })
    | { asChild: true; children: ReactElement<{ className?: string }> }
  >((props, ref) => {
    // @ts-ignore Typescript doesn't get it for some reason
    if (props.asChild) {
      return <Slot {...props} className={classNames(...additionalClassNames)} />;
    } else {
      return React.createElement(typ, {
        ...additionalProps,
        ...props,
        className: classNames((props as any).className, ...additionalClassNames),
        ref,
      });
    }
  });

export default withClasses;
