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

import { Field, HighlightedField, Hit, ValueField } from "@scm-manager/ui-types";

export const isHighlightedField = (field: Field): field is HighlightedField => {
  return field.highlighted;
};

export const isValueField = (field: Field): field is ValueField => {
  return !field.highlighted;
};

export const useFieldValue = (hit: Hit, fieldName: string) => {
  const field = hit.fields[fieldName];
  if (!field) {
    return;
  }
  if (isValueField(field)) {
    return field.value;
  } else {
    throw new Error(`${fieldName} is a highlighted field and not a value field`);
  }
};

export const useStringFieldValue = (hit: Hit, fieldName: string): string | undefined => {
  const value = useFieldValue(hit, fieldName);
  if (value) {
    if (typeof value === "string") {
      return value;
    } else {
      throw new Error("field value is not a string");
    }
  }
};

export const useDateFieldValue = (hit: Hit, fieldName: string) => {
  const value = useFieldValue(hit, fieldName);
  if (value) {
    if (typeof value === "number") {
      return new Date(value);
    } else {
      throw new Error("field value is not a number");
    }
  }
};
