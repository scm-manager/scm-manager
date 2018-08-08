// @flow
import * as generalValidator from "../../../components/validation";

export const isNameValid = (name: string) => {
  return generalValidator.isNameValid(name);
};

export function isContactValid(mail: string) {
  return "" === mail || generalValidator.isMailValid(mail);
}
