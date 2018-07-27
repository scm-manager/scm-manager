// @flow

const nameRegex = /^([A-z0-9.\-_@]|[^ ]([A-z0-9.\-_@ ]*[A-z0-9.\-_@]|[^\s])?)$/;

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};

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
