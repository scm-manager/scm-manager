import * as validator from './validation';

describe('test name validation', () => {
  // invalid names taken from ValidationUtilTest.java
  const invalidNames = [
    '@test',
    ' test 123',
    ' test 123 ',
    'test 123 ',
    'test/123',
    'test%123',
    'test:123',
    't ',
    ' t',
    ' t ',
    '',
    ' invalid_name',
    'another%one',
    '!!!',
    '!_!',
  ];
  for (let name of invalidNames) {
    it(`should return false for '${name}'`, () => {
      expect(validator.isNameValid(name)).toBe(false);
    });
  }

  // valid names taken from ValidationUtilTest.java
  const validNames = [
    'test',
    'test.git',
    'Test123.git',
    'Test123-git',
    'Test_user-123.git',
    'test@scm-manager.de',
    'test123',
    'tt',
    't',
    'valid_name',
    'another1',
    'stillValid',
    'this.one_as-well',
    'and@this',
  ];
  for (let name of validNames) {
    it(`should return true for '${name}'`, () => {
      expect(validator.isNameValid(name)).toBe(true);
    });
  }
});

describe('test mail validation', () => {
  // invalid taken from ValidationUtilTest.java
  const invalid = [
    'ostfalia.de',
    '@ostfalia.de',
    's.sdorra@',
    's.sdorra@ostfalia',
    's.sdorra@ ostfalia.de',
    's.sdorra@[ostfalia.de',
  ];
  for (let mail of invalid) {
    it(`should return false for '${mail}'`, () => {
      expect(validator.isMailValid(mail)).toBe(false);
    });
  }

  // valid taken from ValidationUtilTest.java
  const valid = [
    's.sdorra@ostfalia.de',
    'sdorra@ostfalia.de',
    's.sdorra@hbk-bs.de',
    's.sdorra@gmail.com',
    's.sdorra@t.co',
    's.sdorra@ucla.college',
    's.sdorra@example.xn--p1ai',
    's.sdorra@scm.solutions',
    "s'sdorra@scm.solutions",
    '"S Sdorra"@scm.solutions',
  ];
  for (let mail of valid) {
    it(`should return true for '${mail}'`, () => {
      expect(validator.isMailValid(mail)).toBe(true);
    });
  }
});

describe('test number validation', () => {
  const invalid = ['1a', '35gu', 'dj6', '45,5', 'test'];
  for (let number of invalid) {
    it(`should return false for '${number}'`, () => {
      expect(validator.isNumberValid(number)).toBe(false);
    });
  }
  const valid = ['1', '35', '2', '235', '34.4'];
  for (let number of valid) {
    it(`should return true for '${number}'`, () => {
      expect(validator.isNumberValid(number)).toBe(true);
    });
  }
});

describe('test path validation', () => {
  const invalid = ['//', 'some//path', 'end//'];
  for (let path of invalid) {
    it(`should return false for '${path}'`, () => {
      expect(validator.isPathValid(path)).toBe(false);
    });
  }
  const valid = ['', '/', 'dir', 'some/path', 'end/'];
  for (let path of valid) {
    it(`should return true for '${path}'`, () => {
      expect(validator.isPathValid(path)).toBe(true);
    });
  }
});
