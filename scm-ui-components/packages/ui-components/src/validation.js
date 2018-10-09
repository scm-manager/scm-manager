// @flow
const nameRegex = /^[A-Za-z0-9\.\-_][A-Za-z0-9\.\-_@]*$/;

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};

const mailRegex = /^[A-z0-9][\w.-]*@[A-z0-9][\w\-.]*\.[A-z0-9][A-z0-9-]+$/;

export const isMailValid = (mail: string) => {
  return mailRegex.test(mail);
};

export const isNumberValid = (number: string) => {
  return !isNaN(number);
};
