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

import React, { ReactElement } from "react";
import { Path, PathValue } from "react-hook-form";
import { useScmFormContext } from "../ScmFormContext";
import { ScmFormPathContextProvider, useScmFormPathContext } from "../FormPathContext";
import { Button } from "@scm-manager/ui-buttons";
import { prefixWithoutIndices } from "../helpers";
import classNames from "classnames";
import { useScmFormListContext } from "../ScmFormListContext";
import { useTranslation } from "react-i18next";
import { Notification } from "@scm-manager/ui-components";

type RenderProps<T extends Record<string, unknown>, PATH extends Path<T>> = {
  value: PathValue<T, PATH>;
  index: number;
  remove: () => void;
};

type Props<T extends Record<string, unknown>, PATH extends Path<T>> = {
  withDelete?: boolean;
  children: ((renderProps: RenderProps<T, PATH>) => React.ReactNode | React.ReactNode[]) | React.ReactNode;
  className?: string;
};

/**
 * @beta
 * @since 2.43.0
 */
function ControlledTable<T extends Record<string, unknown>, PATH extends Path<T>>({
  withDelete,
  children,
  className,
}: Props<T, PATH>) {
  const [defaultTranslate] = useTranslation("commons", { keyPrefix: "form.table" });
  const { readOnly, t } = useScmFormContext();
  const nameWithPrefix = useScmFormPathContext();
  const prefixedNameWithoutIndices = prefixWithoutIndices(nameWithPrefix);
  const { fields, remove } = useScmFormListContext();
  const deleteLabel = t(`${prefixedNameWithoutIndices}.delete`) || defaultTranslate("delete.label");
  const emptyTableLabel = t(`${prefixedNameWithoutIndices}.empty`) || defaultTranslate("empty.label");
  const actionHeaderLabel = t(`${prefixedNameWithoutIndices}.action.label`) || defaultTranslate("headers.action.label");

  return (
    <table className={classNames("table content is-hoverable", className)}>
      <thead>
        <tr>
          {React.Children.map(children, (child) => (
            <th>{t(`${prefixedNameWithoutIndices}.${(child as ReactElement).props.name}.label`)}</th>
          ))}
          {withDelete && !readOnly ? <th className="has-text-right">{actionHeaderLabel}</th> : null}
        </tr>
      </thead>
      <tbody>
      {fields.length === 0 ? <tr><td colSpan={1000}><Notification type="info">{emptyTableLabel}</Notification></td></tr> : null}
        {fields.map((value, index) => (
          <ScmFormPathContextProvider key={value.id} path={`${nameWithPrefix}.${index}`}>
            <tr>
              {children}
              {withDelete && !readOnly ? (
                <td className="has-text-right">
                  <Button className="px-4" onClick={() => remove(index)} aria-label={deleteLabel}>
                    <span className="icon is-small">
                      <i className="fas fa-trash" />
                    </span>
                  </Button>
                </td>
              ) : null}
            </tr>
          </ScmFormPathContextProvider>
        ))}
      </tbody>
    </table>
  );
}

export default ControlledTable;
