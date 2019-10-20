import { contextPath } from "./urls";
import {
  createBackendError,
  ForbiddenError,
  isBackendError,
  UnauthorizedError
} from "./errors";
import { BackendErrorContent } from "./errors";

const applyFetchOptions: (p: RequestInit) => RequestInit = o => {
  o.credentials = "same-origin";
  o.headers = {
    Cache: "no-cache",
    // identify the request as ajax request
    "X-Requested-With": "XMLHttpRequest"
  };
  return o;
};

function handleFailure(response: Response) {
  if (!response.ok) {
    if (isBackendError(response)) {
      return response.json().then((content: BackendErrorContent) => {
        throw createBackendError(content, response.status);
      });
    } else {
      if (response.status === 401) {
        throw new UnauthorizedError("Unauthorized", 401);
      } else if (response.status === 403) {
        throw new ForbiddenError("Forbidden", 403);
      }

      throw new Error("server returned status code " + response.status);
    }
  }
  return response;
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
    return fetch(createUrl(url), applyFetchOptions({})).then(handleFailure);
  }

  post(url: string, payload: any, contentType: string = "application/json") {
    return this.httpRequestWithJSONBody("POST", url, contentType, payload);
  }

  postBinary(url: string, fileAppender: (p: FormData) => void) {
    let formData = new FormData();
    fileAppender(formData);

    let options: RequestInit = {
      method: "POST",
      body: formData
    };
    return this.httpRequestWithBinaryBody(options, url);
  }

  put(url: string, payload: any, contentType: string = "application/json") {
    return this.httpRequestWithJSONBody("PUT", url, contentType, payload);
  }

  head(url: string) {
    let options: RequestInit = {
      method: "HEAD"
    };
    options = applyFetchOptions(options);
    return fetch(createUrl(url), options).then(handleFailure);
  }

  delete(url: string): Promise<Response> {
    let options: RequestInit = {
      method: "DELETE"
    };
    options = applyFetchOptions(options);
    return fetch(createUrl(url), options).then(handleFailure);
  }

  httpRequestWithJSONBody(
    method: string,
    url: string,
    contentType: string,
    payload: any
  ): Promise<Response> {
    let options: RequestInit = {
      method: method,
      body: JSON.stringify(payload)
    };
    return this.httpRequestWithBinaryBody(options, url, contentType);
  }

  httpRequestWithBinaryBody(
    options: RequestInit,
    url: string,
    contentType?: string
  ) {
    options = applyFetchOptions(options);
    if (contentType) {
      if (!options.headers) {
        options.headers = new Headers();
      }
      // @ts-ignore
      options.headers["Content-Type"] = contentType;
    }

    return fetch(createUrl(url), options).then(handleFailure);
  }
}

export let apiClient = new ApiClient();
