// @flow

import { isNameValid, isMailValid } from "../../components/validation";

export { isNameValid, isMailValid };

export const isDisplayNameValid = (displayName: string) => {
  if (displayName) {
    return true;
  }
  return false;
};
export const isPasswordValid = (password: string) => {
  return password.length > 6 && password.length < 32;
};
