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

import React from "react";
import { Path, PathValue } from "react-hook-form";
import { useScmFormContext } from "../ScmFormContext";
import { ScmFormPathContextProvider, useScmFormPathContext } from "../FormPathContext";
import { prefixWithoutIndices } from "../helpers";
import { useScmFormListContext } from "../ScmFormListContext";
import { useTranslation } from "react-i18next";
import { Button } from "../../buttons";

type ArrayItemType<T> = T extends Array<infer U> ? U : unknown;

type RenderProps<T extends Record<string, unknown>, PATH extends Path<T>> = {
  value: ArrayItemType<PathValue<T, PATH>>;
  index: number;
  remove: () => void;
};

type Props<T extends Record<string, unknown>, PATH extends Path<T>> = {
  withDelete?: boolean;
  children: ((renderProps: RenderProps<T, PATH>) => React.ReactNode | React.ReactNode[]) | React.ReactNode;
};

/**
 * @beta
 * @since 2.43.0
 */
function ControlledList<T extends Record<string, unknown>, PATH extends Path<T>>({
  withDelete,
  children,
}: Props<T, PATH>) {
  const [defaultTranslate] = useTranslation("commons", { keyPrefix: "form" });
  const { readOnly, t } = useScmFormContext();
  const nameWithPrefix = useScmFormPathContext();
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const { fields, remove } = useScmFormListContext();

  const deleteButtonTranslation = t(`${prefixedNameWithoutIndices}.delete`, {
    defaultValue: defaultTranslate("list.delete.label", {
      entity: t(`${prefixedNameWithoutIndices}.entity`),
    }),
  });

  return (
    <>
      {fields.map((value, index) => (
        <ScmFormPathContextProvider key={value.id} path={`${nameWithPrefix}.${index}`}>
          {typeof children === "function"
            ? children({ value: value as never, index, remove: () => remove(index) })
            : children}
          {withDelete && !readOnly ? (
            <div className="level-right">
              <Button variant="signal" onClick={() => remove(index)}>
                {deleteButtonTranslation}
              </Button>
            </div>
          ) : null}
          <hr />
        </ScmFormPathContextProvider>
      ))}
    </>
  );
}

export default ControlledList;
