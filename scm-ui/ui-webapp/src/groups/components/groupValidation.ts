import { validation } from '@scm-manager/ui-components';

const isNameValid = validation.isNameValid;

export { isNameValid };

export const isMemberNameValid = (name: string) => {
  return isNameValid(name);
};
