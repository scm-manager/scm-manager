// @flow

// get api base url from environment
const apiUrl = process.env.API_URL || process.env.PUBLIC_URL || "";

export const PAGE_NOT_FOUND_ERROR = Error("page not found");

// fetch does not send the X-Requested-With header (https://github.com/github/fetch/issues/17),
// but we need the header to detect ajax request (AjaxAwareAuthenticationRedirectStrategy).
const fetchOptions: RequestOptions = {
  credentials: "same-origin",
  headers: {
    "X-Requested-With": "XMLHttpRequest"
  }
};

function handleStatusCode(response: Response) {
  if (!response.ok) {
    if (response.status === 401) {
      return response;
    }
    if (response.status === 404) {
      throw PAGE_NOT_FOUND_ERROR;
    }
    throw new Error("server returned status code " + response.status);
  }
  return response;
}

function createUrl(url: string) {
  return `${apiUrl}/api/rest/v2/${url}`;
}

class ApiClient {
  get(url: string) {
    return fetch(createUrl(url), fetchOptions).then(handleStatusCode);
  }

  post(url: string, payload: any) {
    return this.httpRequestWithJSONBody(url, payload, "POST");
  }

  delete(url: string, payload: any) {
    let options: RequestOptions = {
      method: "DELETE"
    };
    options = Object.assign(options, fetchOptions);
    return fetch(createUrl(url), options).then(handleStatusCode);
  }

  httpRequestWithJSONBody(url: string, payload: any, method: string) {
    let options: RequestOptions = {
      method: method,
      body: JSON.stringify(payload)
    };
    options = Object.assign(options, fetchOptions);
    // $FlowFixMe
    options.headers["Content-Type"] = "application/json";

    return fetch(createUrl(url), options).then(handleStatusCode);
  }
}

export let apiClient = new ApiClient();
