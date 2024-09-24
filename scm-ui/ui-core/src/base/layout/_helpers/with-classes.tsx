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
