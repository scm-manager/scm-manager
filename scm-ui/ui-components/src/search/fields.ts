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
