// @flow

// get api base url from environment
const apiUrl = process.env.API_URL || process.env.PUBLIC_URL || "/scm";

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
    if (response.status === 401) {
      throw UNAUTHORIZED_ERROR;
    }
    if (response.status === 404) {
      throw NOT_FOUND_ERROR;
    }
    throw new Error("server returned status code " + response.status);
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
  return `${apiUrl}/api/rest/v2${urlWithStartingSlash}`;
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
