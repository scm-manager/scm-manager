/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
