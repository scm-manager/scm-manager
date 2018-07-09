// @flow
import reducer, {
  LOGIN_REQUEST,
  LOGIN_FAILED,
  IS_AUTHENTICATED,
  IS_NOT_AUTHENTICATED
} from "./login";
import { LOGIN, LOGIN_SUCCESSFUL } from "./login";

test("login", () => {
  var newState = reducer({}, { type: LOGIN });
  expect(newState.login).toBe(false);
  expect(newState.error).toBe(null);
});

test("login request", () => {
  var newState = reducer({}, { type: LOGIN_REQUEST });
  expect(newState.login).toBe(undefined);
});

test("login successful", () => {
  var newState = reducer({ login: false }, { type: LOGIN_SUCCESSFUL });
  expect(newState.login).toBe(true);
  expect(newState.error).toBe(null);
});

test("login failed", () => {
  var newState = reducer({}, { type: LOGIN_FAILED, payload: "error!" });
  expect(newState.login).toBe(false);
  expect(newState.error).toBe("error!");
});

test("is authenticated", () => {
  var newState = reducer(
    { login: false },
    { type: IS_AUTHENTICATED, username: "test" }
  );
  expect(newState.login).toBeTruthy();
  expect(newState.username).toBe("test");
});

test("is not authenticated", () => {
  var newState = reducer(
    { login: true, username: "foo" },
    { type: IS_NOT_AUTHENTICATED }
  );
  expect(newState.login).toBe(false);
  expect(newState.username).toBeNull();
});
