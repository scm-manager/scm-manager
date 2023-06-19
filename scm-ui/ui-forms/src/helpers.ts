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
