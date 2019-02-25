// @flow
type Context = { type: string, id: string }[];

export type BackendErrorContent = {
  transactionId: string,
  errorCode: string,
  message: string,
  url?: string,
  context: Context
};

export class BackendError extends Error {
  transactionId: string;
  errorCode: string;
  url: ?string;
  context: Context = [];
  statusCode: number;

  constructor(content: BackendErrorContent, name: string, statusCode: number) {
    super(content.message);
    this.name = name;
    this.transactionId = content.transactionId;
    this.errorCode = content.errorCode;
    this.url = content.url;
    this.context = content.context;
    this.statusCode = statusCode;
  }
}

export class UnauthorizedError extends Error {
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
export function createBackendError(
  content: BackendErrorContent,
  statusCode: number
) {
  switch (statusCode) {
    case 404:
      return new NotFoundError(content, statusCode);
    default:
      return new BackendError(content, "BackendError", statusCode);
  }
}

export function isBackendError(response: Response) {
  return response.headers.get("Content-Type") === "application/vnd.scmm-error+json;v=2";
}
