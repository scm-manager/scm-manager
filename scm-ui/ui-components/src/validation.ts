const nameRegex = /^[A-Za-z0-9\.\-_][A-Za-z0-9\.\-_@]*$/;

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};

const mailRegex = /^[ -~]+@[A-Za-z0-9][\w\-.]*\.[A-Za-z0-9][A-Za-z0-9-]+$/;

export const isMailValid = (mail: string) => {
  return mailRegex.test(mail);
};

export const isNumberValid = (number: any) => {
  return !isNaN(number);
};

const pathRegex = /^((?!\/{2,}).)*$/;

export const isPathValid = (path: string) => {
  return pathRegex.test(path);
};
