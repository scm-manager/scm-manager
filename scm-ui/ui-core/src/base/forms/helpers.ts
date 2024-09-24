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

import { UseFormReturn } from "react-hook-form";
import { ForwardedRef, forwardRef, MutableRefObject, Ref, RefCallback } from "react";

export function prefixWithoutIndices(path: string): string {
  return path.replace(/(\.\d+)/g, "");
}

/**
 * Works like {@link setValue} but recursively applies the whole {@link newValues} object.
 *
 * > *Important Note:* This deeply overwrites input values of fields in arrays,
 * > but does **NOT** add or remove items to existing arrays.
 * > This can therefore not be used to clear lists.
 */
export function setValues<T>(newValues: T, setValue: UseFormReturn<T>["setValue"], path = "") {
  for (const [key, val] of Object.entries(newValues)) {
    if (val !== null && typeof val === "object") {
      if (Array.isArray(val)) {
        val.forEach((subVal, idx) => setValues(subVal, setValue, path ? `${path}.${key}.${idx}` : `${key}.${idx}`));
      } else {
        setValues(val, setValue, path ? `${path}.${key}` : key);
      }
    } else {
      const fullPath = path ? `${path}.${key}` : key;
      setValue(fullPath as any, val, { shouldValidate: !fullPath.endsWith("Confirmation"), shouldDirty: true });
    }
  }
}

export function withForwardRef<T extends { name: string }>(component: T): T {
  return forwardRef(component as unknown as any) as any;
}

export const defaultOptionFactory = (item: any) =>
  typeof item === "object" && item !== null && "value" in item && typeof item["label"] === "string"
    ? item
    : { label: item as string, value: item };

export function mergeRefs<T>(...refs: Array<RefCallback<T> | MutableRefObject<T> | ForwardedRef<T>>) {
  return (el: T) =>
    refs.forEach((ref) => {
      if (ref) {
        if (typeof ref === "function") {
          ref(el);
        } else {
          ref.current = el;
        }
      }
    });
}
