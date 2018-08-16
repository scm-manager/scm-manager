// @flow
const nameRegex = /^([A-z0-9.\-_@]|[^ ]([A-z0-9.\-_@ ]*[A-z0-9.\-_@]|[^\s])?)$/;

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

const urlRegex = new RegExp(
  "^(https?:\\/\\/)?" + // protocol
  "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|" + // domain name
  "((\\d{1,3}\\.){3}\\d{1,3}))" + // OR ip (v4) address
  "(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*" + // port and path
  "(\\?[;&a-z\\d%_.~+=-]*)?" + // query string
    "(\\#[-a-z\\d_]*)?$",
  "i"
); // fragment locator

export const isUrlValid = (url: string) => {
  return urlRegex.test(url);
};
