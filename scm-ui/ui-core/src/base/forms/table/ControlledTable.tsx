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

import React, { ReactElement } from "react";
import { Path, PathValue } from "react-hook-form";
import { useScmFormContext } from "../ScmFormContext";
import { ScmFormPathContextProvider, useScmFormPathContext } from "../FormPathContext";
import { Button } from "../../buttons";
import { prefixWithoutIndices } from "../helpers";
import classNames from "classnames";
import { useScmFormListContext } from "../ScmFormListContext";
import { useTranslation } from "react-i18next";
import { Notification } from "../../notifications";

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
