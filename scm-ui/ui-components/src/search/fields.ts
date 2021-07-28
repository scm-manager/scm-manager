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

import { HitField, HighlightedHitField, Hit, ValueHitField } from "@scm-manager/ui-types";

export const isHighlightedHitField = (field: HitField): field is HighlightedHitField => {
  return field.highlighted;
};

export const isValueHitField = (field: HitField): field is ValueHitField => {
  return !field.highlighted;
};

export const useHitFieldValue = (hit: Hit, fieldName: string) => {
  const field = hit.fields[fieldName];
  if (!field) {
    return;
  }
  if (isValueHitField(field)) {
    return field.value;
  } else {
    throw new Error(`${fieldName} is a highlighted field and not a value field`);
  }
};

export const useStringHitFieldValue = (hit: Hit, fieldName: string): string | undefined => {
  const value = useHitFieldValue(hit, fieldName);
  if (value) {
    if (typeof value === "string") {
      return value;
    } else {
      throw new Error(`field value of ${fieldName} is not a string`);
    }
  }
};

export const useNumberHitFieldValue = (hit: Hit, fieldName: string): number | undefined => {
  const value = useHitFieldValue(hit, fieldName);
  if (value) {
    if (typeof value === "number") {
      return value;
    } else {
      throw new Error(`field value of ${fieldName} is not a number`);
    }
  }
};

export const useDateHitFieldValue = (hit: Hit, fieldName: string): Date | undefined => {
  const value = useNumberHitFieldValue(hit, fieldName);
  if (value) {
    return new Date(value);
  }
};

export const useBooleanHitFieldValue = (hit: Hit, fieldName: string): boolean | undefined => {
  const value = useHitFieldValue(hit, fieldName);
  if (value) {
    if (typeof value === "boolean") {
      return value;
    } else {
      throw new Error(`field value of ${fieldName} is not a boolean`);
    }
  }
};
