// @flow

//TODO: How should a group be validated
//TODO: Tests!

const nameRegex = /^([A-z0-9.\-_@]|[^ ]([A-z0-9.\-_@ ]*[A-z0-9.\-_@]|[^\s])?)$/;

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};
