import { contextPath } from "./urls";
import { createBackendError, ForbiddenError, isBackendError, UnauthorizedError } from "./errors";
import { BackendErrorContent } from "./errors";

const sessionId = (
  Date.now().toString(36) +
  Math.random()
    .toString(36)
    .substr(2, 5)
).toUpperCase();

const extractXsrfTokenFromJwt = (jwt: string) => {
  const parts = jwt.split(".");
  if (parts.length === 3) {
    return JSON.parse(atob(parts[1])).xsrf;
  }
};

// @VisibleForTesting
export const extractXsrfTokenFromCookie = (cookieString?: string) => {
  if (cookieString) {
    const cookies = cookieString.split(";");
    for (const c of cookies) {
      const parts = c.trim().split("=");
      if (parts[0] === "X-Bearer-Token") {
        return extractXsrfTokenFromJwt(parts[1]);
      }
    }
  }
};

const extractXsrfToken = () => {
  return extractXsrfTokenFromCookie(document.cookie);
};

const applyFetchOptions: (p: RequestInit) => RequestInit = o => {
  if (!o.headers) {
    o.headers = {};
  }

  // @ts-ignore We are sure that here we only get headers of type Record<string, string>
  const headers: Record<string, string> = o.headers;
  headers["Cache"] = "no-cache";
  // identify the request as ajax request
  headers["X-Requested-With"] = "XMLHttpRequest";
  // identify the web interface
  headers["X-SCM-Client"] = "WUI";
  // identify the window session
  headers["X-SCM-Session-ID"] = sessionId

  const xsrf = extractXsrfToken();
  if (xsrf) {
    headers["X-XSRF-Token"] = xsrf;
  }

  o.credentials = "same-origin";
  o.headers = headers;
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

  post(url: string, payload?: any, contentType = "application/json", additionalHeaders: Record<string, string> = {}) {
    return this.httpRequestWithJSONBody("POST", url, contentType, additionalHeaders, payload);
  }

  postText(url: string, payload: string, additionalHeaders: Record<string, string> = {}) {
    return this.httpRequestWithTextBody("POST", url, additionalHeaders, payload);
  }

  putText(url: string, payload: string, additionalHeaders: Record<string, string> = {}) {
    return this.httpRequestWithTextBody("PUT", url, additionalHeaders, payload);
  }

  postBinary(url: string, fileAppender: (p: FormData) => void, additionalHeaders: Record<string, string> = {}) {
    const formData = new FormData();
    fileAppender(formData);

    const options: RequestInit = {
      method: "POST",
      body: formData,
      headers: additionalHeaders
    };
    return this.httpRequestWithBinaryBody(options, url);
  }

  put(url: string, payload: any, contentType = "application/json", additionalHeaders: Record<string, string> = {}) {
    return this.httpRequestWithJSONBody("PUT", url, contentType, additionalHeaders, payload);
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
    additionalHeaders: Record<string, string>,
    payload?: any
  ): Promise<Response> {
    const options: RequestInit = {
      method: method,
      headers: additionalHeaders
    };
    if (payload) {
      options.body = JSON.stringify(payload);
    }
    return this.httpRequestWithBinaryBody(options, url, contentType);
  }

  httpRequestWithTextBody(
    method: string,
    url: string,
    additionalHeaders: Record<string, string> = {},
    payload: string
  ) {
    const options: RequestInit = {
      method: method,
      headers: additionalHeaders
    };
    options.body = payload;
    return this.httpRequestWithBinaryBody(options, url, "text/plain");
  }

  httpRequestWithBinaryBody(options: RequestInit, url: string, contentType?: string) {
    options = applyFetchOptions(options);
    if (contentType) {
      if (!options.headers) {
        options.headers = {};
      }
      // @ts-ignore We are sure that here we only get headers of type Record<string, string>
      options.headers["Content-Type"] = contentType;
    }

    return fetch(createUrl(url), options).then(handleFailure);
  }
}

export const apiClient = new ApiClient();
