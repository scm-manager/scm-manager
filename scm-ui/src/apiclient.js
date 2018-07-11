// @flow

// get api base url from environment
const apiUrl = process.env.API_URL || process.env.PUBLIC_URL || "/scm";

export const PAGE_NOT_FOUND_ERROR = Error("page not found");
export const NOT_AUTHENTICATED_ERROR = Error("not authenticated");

const fetchOptions: RequestOptions = {
  credentials: "same-origin",
  headers: {
    Cache: "no-cache"
  }
};

function handleStatusCode(response: Response) {
  if (!response.ok) {
    if (response.status === 401) {
      throw NOT_AUTHENTICATED_ERROR;
    }
    if (response.status === 404) {
      throw PAGE_NOT_FOUND_ERROR;
    }
    throw new Error("server returned status code " + response.status);
  }
  return response;
}

function createUrl(url: string) {
  if (url.indexOf("://") > 0) {
    return url;
  }
  return `${apiUrl}/api/rest/v2/${url}`;
}

class ApiClient {
  get(url: string): Promise<Response> {
    return fetch(createUrl(url), fetchOptions).then(handleStatusCode);
  }

  post(url: string, payload: any) {
    return this.httpRequestWithJSONBody(url, payload, "POST");
  }

  postWithContentType(url: string, payload: any, contentType: string) {
    return this.httpRequestWithContentType(
      url,
      "POST",
      JSON.stringify(payload),
      contentType
    );
  }

  putWithContentType(url: string, payload: any, contentType: string) {
    return this.httpRequestWithContentType(
      url,
      "PUT",
      JSON.stringify(payload),
      contentType
    );
  }

  delete(url: string): Promise<Response> {
    let options: RequestOptions = {
      method: "DELETE"
    };
    options = Object.assign(options, fetchOptions);
    return fetch(createUrl(url), options).then(handleStatusCode);
  }

  httpRequestWithJSONBody(
    url: string,
    payload: any,
    method: string
  ): Promise<Response> {
    // let options: RequestOptions = {
    //   method: method,
    //   body: JSON.stringify(payload)
    // };
    // options = Object.assign(options, fetchOptions);
    // // $FlowFixMe
    // options.headers["Content-Type"] = "application/json";

    // return fetch(createUrl(url), options).then(handleStatusCode);

    return this.httpRequestWithContentType(
      url,
      method,
      JSON.stringify(payload),
      "application/json"
    ).then(handleStatusCode);
  }

  httpRequestWithContentType(
    url: string,
    method: string,
    payload: any,
    contentType: string
  ): Promise<Response> {
    let options: RequestOptions = {
      method: method,
      body: payload
    };
    options = Object.assign(options, fetchOptions);
    // $FlowFixMe
    options.headers["Content-Type"] = contentType;

    return fetch(createUrl(url), options).then(handleStatusCode);
  }
}

export let apiClient = new ApiClient();
