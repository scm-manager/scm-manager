/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { contextPath } from "./urls";
import {
  BackendErrorContent,
  BadGatewayError,
  createBackendError,
  ForbiddenError,
  isBackendError,
  TOKEN_EXPIRED_ERROR_CODE,
  TokenExpiredError,
  UnauthorizedError,
} from "./errors";

type SubscriptionEvent = {
  type: string;
};

type OpenEvent = SubscriptionEvent;

type ErrorEvent = SubscriptionEvent & {
  error: Error;
};

type MessageEvent = SubscriptionEvent & {
  data: string;
  lastEventId?: string;
};

type MessageListeners = {
  [eventType: string]: (event: MessageEvent) => void;
};

type SubscriptionContext = {
  onOpen?: OpenEvent;
  onMessage: MessageListeners;
  onError?: ErrorEvent;
};

type SubscriptionArgument = MessageListeners | SubscriptionContext;

type Cancel = () => void;

const sessionId = (Date.now().toString(36) + Math.random().toString(36).substr(2, 5)).toUpperCase();

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
      if (parts[0] === "X-Bearer-Token" || parts[0] === "X-SCM-Init-Token") {
        return extractXsrfTokenFromJwt(parts[1]);
      }
    }
  }
};

const extractXsrfToken = () => {
  return extractXsrfTokenFromCookie(document.cookie);
};

const createRequestHeaders = () => {
  const headers: { [key: string]: string } = {
    // disable caching for now
    Cache: "no-cache",
    // identify the request as ajax request
    "X-Requested-With": "XMLHttpRequest",
    // identify the web interface
    "X-SCM-Client": "WUI",
    // identify the window session
    "X-SCM-Session-ID": sessionId,
  };

  const xsrf = extractXsrfToken();
  if (xsrf) {
    headers["X-XSRF-Token"] = xsrf;
  }
  return headers;
};

const applyFetchOptions: (p: RequestInit) => RequestInit = (o) => {
  if (o.headers) {
    o.headers = {
      ...createRequestHeaders(),
    };
  } else {
    o.headers = createRequestHeaders();
  }
  o.credentials = "same-origin";
  return o;
};

function handleFailure(response: Response) {
  if (!response.ok) {
    if (response.status === 401) {
      if (isBackendError(response)) {
        return response.json().then((content: BackendErrorContent) => {
          if (content.errorCode === TOKEN_EXPIRED_ERROR_CODE) {
            throw new TokenExpiredError("Token expired", 401);
          } else {
            throw new UnauthorizedError("Unauthorized", 401);
          }
        });
      }
      throw new UnauthorizedError("Unauthorized", 401);
    } else if (response.status === 403) {
      throw new ForbiddenError("Forbidden", 403);
    } else if (response.status === 502) {
      throw new BadGatewayError("Bad Gateway", 502);
    } else if (isBackendError(response)) {
      return response.json().then((content: BackendErrorContent) => {
        throw createBackendError(content, response.status);
      });
    } else {
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

export function createUrlWithIdentifiers(url: string): string {
  return createUrl(url) + "?X-SCM-Client=WUI&X-SCM-Session-ID=" + sessionId;
}

type ErrorListener = (error: Error) => void;

type RequestListener = (url: string, options?: RequestInit) => void;

class ApiClient {
  errorListeners: ErrorListener[] = [];
  requestListeners: RequestListener[] = [];

  get = (url: string): Promise<Response> => {
    return this.request(url, applyFetchOptions({})).then(handleFailure).catch(this.notifyAndRethrow);
  };

  post = (
    url: string,
    payload?: any,
    contentType = "application/json",
    additionalHeaders: Record<string, string> = {}
  ) => {
    return this.httpRequestWithJSONBody("POST", url, contentType, additionalHeaders, payload);
  };

  postText = (url: string, payload: string, additionalHeaders: Record<string, string> = {}) => {
    return this.httpRequestWithTextBody("POST", url, additionalHeaders, payload);
  };

  putText = (url: string, payload: string, additionalHeaders: Record<string, string> = {}) => {
    return this.httpRequestWithTextBody("PUT", url, additionalHeaders, payload);
  };

  postBinary = (url: string, fileAppender: (p: FormData) => void, additionalHeaders: Record<string, string> = {}) => {
    const formData = new FormData();
    fileAppender(formData);

    const options: RequestInit = {
      method: "POST",
      body: formData,
      headers: additionalHeaders,
    };
    return this.httpRequestWithBinaryBody(options, url);
  };

  putBinary = (url: string, fileAppender: (p: FormData) => void, additionalHeaders: Record<string, string> = {}) => {
    const formData = new FormData();
    fileAppender(formData);

    const options: RequestInit = {
      method: "PUT",
      body: formData,
      headers: additionalHeaders,
    };
    return this.httpRequestWithBinaryBody(options, url);
  };

  put(url: string, payload: any, contentType = "application/json", additionalHeaders: Record<string, string> = {}) {
    return this.httpRequestWithJSONBody("PUT", url, contentType, additionalHeaders, payload);
  }

  head = (url: string) => {
    let options: RequestInit = {
      method: "HEAD",
    };
    options = applyFetchOptions(options);
    return this.request(url, options).then(handleFailure).catch(this.notifyAndRethrow);
  };

  delete = (url: string): Promise<Response> => {
    let options: RequestInit = {
      method: "DELETE",
    };
    options = applyFetchOptions(options);
    return this.request(url, options).then(handleFailure).catch(this.notifyAndRethrow);
  };

  httpRequestWithJSONBody = (
    method: string,
    url: string,
    contentType: string,
    additionalHeaders: Record<string, string>,
    payload?: any
  ): Promise<Response> => {
    const options: RequestInit = {
      method: method,
      headers: additionalHeaders,
    };
    if (payload) {
      options.body = JSON.stringify(payload);
    }
    return this.httpRequestWithBinaryBody(options, url, contentType);
  };

  httpRequestWithTextBody = (
    method: string,
    url: string,
    additionalHeaders: Record<string, string> = {},
    payload: string
  ) => {
    const options: RequestInit = {
      method: method,
      headers: additionalHeaders,
    };
    options.body = payload;
    return this.httpRequestWithBinaryBody(options, url, "text/plain");
  };

  httpRequestWithBinaryBody = (options: RequestInit, url: string, contentType?: string) => {
    options = applyFetchOptions(options);
    if (contentType) {
      if (!options.headers) {
        options.headers = {};
      }
      // @ts-ignore We are sure that here we only get headers of type {[name:string]: string}
      options.headers["Content-Type"] = contentType;
    }

    return this.request(url, options).then(handleFailure).catch(this.notifyAndRethrow);
  };

  subscribe(url: string, argument: SubscriptionArgument): Cancel {
    const es = new EventSource(createUrlWithIdentifiers(url), {
      withCredentials: true,
    });

    let listeners: MessageListeners;
    // type guard, to identify that argument is of type SubscriptionContext
    if ("onMessage" in argument) {
      listeners = (argument as SubscriptionContext).onMessage;
      if (argument.onError) {
        // @ts-ignore typing of EventSource is weird
        es.onerror = argument.onError;
      }
      if (argument.onOpen) {
        // @ts-ignore typing of EventSource is weird
        es.onopen = argument.onOpen;
      }
    } else {
      listeners = argument;
    }

    for (const type in listeners) {
      // @ts-ignore typing of EventSource is weird
      es.addEventListener(type, listeners[type]);
    }

    return () => es.close();
  }

  onRequest = (requestListener: RequestListener) => {
    this.requestListeners.push(requestListener);
  };

  onError = (errorListener: ErrorListener) => {
    this.errorListeners.push(errorListener);
  };

  private request = (url: string, options: RequestInit) => {
    this.notifyRequestListeners(url, options);
    return fetch(createUrl(url), options);
  };

  private notifyRequestListeners = (url: string, options: RequestInit) => {
    this.requestListeners.forEach((requestListener) => requestListener(url, options));
  };

  private notifyAndRethrow = (error: Error): never => {
    this.errorListeners.forEach((errorListener) => errorListener(error));
    throw error;
  };
}

export const apiClient = new ApiClient();
