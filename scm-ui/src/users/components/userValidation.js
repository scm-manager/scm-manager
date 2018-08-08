// @flow

import { isNameValid } from "../../components/validation";

export { isNameValid };

export const isDisplayNameValid = (displayName: string) => {
  if (displayName) {
    return true;
  }
  return false;
};

const mailRegex = /^[A-z0-9][\w.-]*@[A-z0-9][\w\-.]*\.[A-z0-9][A-z0-9-]+$/;

export const isMailValid = (mail: string) => {
  return mailRegex.test(mail);
};

export const isPasswordValid = (password: string) => {
  return password.length > 6 && password.length < 32;
};
