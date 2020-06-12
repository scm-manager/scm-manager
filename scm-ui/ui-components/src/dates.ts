import styled from "styled-components";
import { parseISO } from "date-fns";

export type DateInput = Date | string;

export const ShortDateFormat = "yyyy-MM-dd";
export const FullDateFormat = "yyyy-MM-dd HH:mm:ss";

export const DateElement = styled.time`
  border-bottom: 1px dotted rgba(219, 219, 219);
  cursor: help;
`;

export const toDate = (value: DateInput): Date => {
  if (value instanceof Date) {
    return value;
  }
  return parseISO(value);
};
