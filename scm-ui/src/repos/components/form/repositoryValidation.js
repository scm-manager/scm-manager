// @flow
import { validation } from "@scm-manager/ui-components";

export const isNameValid = (name: string) => {
  return validation.isNameValid(name);
};

export function isContactValid(mail: string) {
  return "" === mail || validation.isMailValid(mail);
}
