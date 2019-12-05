const isUndefined = (o: any, key: string, nested?: string) => {
  if (typeof o === "undefined" || typeof o[key] === "undefined") {
    return true;
  }
  if (nested) {
    return typeof o[key][nested] === "undefined";
  }
  return false;
};

export const byKey = (key: string) => {
  return (a: any, b: any) => {
    if (isUndefined(a, key)) {
      return 1;
    }

    if (isUndefined(b, key)) {
      return 0;
    }

    if (a[key] < b[key]) {
      return -1;
    } else if (a[key] > b[key]) {
      return 1;
    } else {
      return 0;
    }
  };
};

export const byValueLength = (key: string) => {
  return (a: any, b: any) => {
    if (isUndefined(a, key)) {
      return 1;
    }

    if (isUndefined(b, key)) {
      return 0;
    }

    if (a[key].length < b[key].length) {
      return -1;
    } else if (a[key].length > b[key].length) {
      return 1;
    } else {
      return 0;
    }
  };
};

export const byNestedKeys = (key: string, nestedKey: string) => {
  return (a: any, b: any) => {
    if (isUndefined(a, key, nestedKey)) {
      return 1;
    }

    if (isUndefined(b, key, nestedKey)) {
      return 0;
    }

    if (a[key][nestedKey] < b[key][nestedKey]) {
      return -1;
    } else if (a[key][nestedKey] > b[key][nestedKey]) {
      return 1;
    } else {
      return 0;
    }
  };
};

export default {
  byKey,
  byValueLength,
  byNestedKeys
};
