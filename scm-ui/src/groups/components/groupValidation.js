// @flow
import { isNameValid } from "../../components/validation";

export { isNameValid };

export const isMemberNameValid = (name: string) => {
  return isNameValid(name);
};
