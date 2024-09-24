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
