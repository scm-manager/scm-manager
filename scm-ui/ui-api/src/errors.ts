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

type Context = {
  type: string;
  id: string;
}[];

export type Violation = {
  path?: string;
  message: string;
  key?: string;
};

export type AdditionalMessage = {
  key?: string;
  message?: string;
};

export type BackendErrorContent = {
  transactionId: string;
  errorCode: string;
  message: string;
  url?: string;
  context: Context;
  violations: Violation[];
  additionalMessages?: AdditionalMessage[];
};

export class BackendError extends Error {
  transactionId: string;
  errorCode: string;
  url: string | null | undefined;
  context: Context = [];
  statusCode: number;
  violations: Violation[];
  additionalMessages?: AdditionalMessage[];

  constructor(content: BackendErrorContent, name: string, statusCode: number) {
    super(content.message);
    this.name = name;
    this.transactionId = content.transactionId;
    this.errorCode = content.errorCode;
    this.url = content.url;
    this.context = content.context;
    this.statusCode = statusCode;
    this.violations = content.violations;
    this.additionalMessages = content.additionalMessages;
  }
}

export class UnauthorizedError extends Error {
  statusCode: number;

  constructor(message: string, statusCode: number) {
    super(message);
    this.statusCode = statusCode;
  }
}

export class BadGatewayError extends Error {
  statusCode: number;

  constructor(message: string, statusCode: number) {
    super(message);
    this.statusCode = statusCode;
  }
}

export class TokenExpiredError extends UnauthorizedError {}

export class ForbiddenError extends Error {
  statusCode: number;

  constructor(message: string, statusCode: number) {
    super(message);
    this.statusCode = statusCode;
  }
}

export class NotFoundError extends BackendError {
  constructor(content: BackendErrorContent, statusCode: number) {
    super(content, "NotFoundError", statusCode);
  }
}

export class ConflictError extends BackendError {
  constructor(content: BackendErrorContent, statusCode: number) {
    super(content, "ConflictError", statusCode);
  }
}

export class MissingLinkError extends Error {
  name = "MissingLinkError";
}

export function createBackendError(content: BackendErrorContent, statusCode: number) {
  switch (statusCode) {
    case 404:
      return new NotFoundError(content, statusCode);
    case 409:
      return new ConflictError(content, statusCode);
    default:
      return new BackendError(content, "BackendError", statusCode);
  }
}

export function isBackendError(response: Response) {
  return response.headers.get("Content-Type") === "application/vnd.scmm-error+json;v=2";
}

export const TOKEN_EXPIRED_ERROR_CODE = "DDS8D8unr1";
