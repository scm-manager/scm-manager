// @flow
import {contextPath} from "./urls";

export const NOT_FOUND_ERROR = Error("not found");
export const UNAUTHORIZED_ERROR = Error("unauthorized");

const fetchOptions: RequestOptions = {
  credentials: "same-origin",
  headers: {
    Cache: "no-cache"
  }
};

function handleStatusCode(response: Response) {
  if (!response.ok) {
    switch (response.status) {
      case 401:
        return throwErrorWithMessage(response, UNAUTHORIZED_ERROR.message);
      case 404:
        return throwErrorWithMessage(response, NOT_FOUND_ERROR.message);
      default:
        return throwErrorWithMessage(response, "server returned status code " + response.status);
    }

  }
  return response;
}

function throwErrorWithMessage(response: Response, message: string) {
  return response.json().then(
    json => {
      throw Error(json.message);
    },
    () => {
      throw Error(message);
    }
  );
}

export function createUrl(url: string) {
  if (url.includes("://")) {
    return url;
  }
  let urlWithStartingSlash = url;
  if (url.indexOf("/") !== 0) {
    urlWithStartingSlash = "/" + urlWithStartingSlash;
  }
  return `${contextPath}/api/v2${urlWithStartingSlash}`;
}

class ApiClient {
  get(url: string): Promise<Response> {
    return fetch(createUrl(url), fetchOptions).then(handleStatusCode);
  }

  post(url: string, payload: any, contentType: string = "application/json") {
    return this.httpRequestWithJSONBody("POST", url, contentType, payload);
  }

  put(url: string, payload: any, contentType: string = "application/json") {
    return this.httpRequestWithJSONBody("PUT", url, contentType, payload);
  }

  head(url: string) {
    let options: RequestOptions = {
      method: "HEAD"
    };
    options = Object.assign(options, fetchOptions);
    return fetch(createUrl(url), options).then(handleStatusCode);
  }

  delete(url: string): Promise<Response> {
    let options: RequestOptions = {
      method: "DELETE"
    };
    options = Object.assign(options, fetchOptions);
    return fetch(createUrl(url), options).then(handleStatusCode);
  }

  httpRequestWithJSONBody(
    method: string,
    url: string,
    contentType: string,
    payload: any
  ): Promise<Response> {
    let options: RequestOptions = {
      method: method,
      body: JSON.stringify(payload)
    };
    options = Object.assign(options, fetchOptions);
    // $FlowFixMe
    options.headers["Content-Type"] = contentType;

    return fetch(createUrl(url), options).then(handleStatusCode);
  }
}

export let apiClient = new ApiClient();
