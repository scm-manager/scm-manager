// @flow
import { validation } from "@scm-manager/ui-components";

const nameRegex = /(?!^\.\.$)(?!^\.$)(?!.*[\\\[\]])^[A-Za-z0-9\.][A-Za-z0-9\.\-_]*$/;

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};

export function isContactValid(mail: string) {
  return "" === mail || validation.isMailValid(mail);
}
