// @create-index

import * as validation from "./validation.js";
import * as urls from "./urls";
import * as repositories from "./repositories.js";

export { validation, urls, repositories };

export { default as DateFromNow } from "./DateFromNow.js";
export { default as ErrorNotification } from "./ErrorNotification.js";
export { default as ErrorPage } from "./ErrorPage.js";
export { default as Image } from "./Image.js";
export { default as Loading } from "./Loading.js";
export { default as Logo } from "./Logo.js";
export { default as MailLink } from "./MailLink.js";
export { default as Notification } from "./Notification.js";
export { default as Paginator } from "./Paginator.js";
export { default as LinkPaginator } from "./LinkPaginator.js";
export { default as StatePaginator } from "./StatePaginator.js";

export { default as ProtectedRoute } from "./ProtectedRoute.js";
export { default as Help } from "./Help";
export { default as HelpIcon } from "./HelpIcon";
export { default as Tooltip } from "./Tooltip";
export { getPageFromMatch } from "./urls";
export { default as Autocomplete} from "./Autocomplete";
export { default as BranchSelector } from "./BranchSelector";
export { default as MarkdownView } from "./MarkdownView";
export { default as SyntaxHighlighter } from "./SyntaxHighlighter";
export { default as ErrorBoundary } from "./ErrorBoundary";

export { apiClient } from "./apiclient.js";
export * from "./errors";

export * from "./avatar";
export * from "./buttons";
export * from "./config";
export * from "./forms";
export * from "./layout";
export * from "./modals";
export * from "./navigation";
export * from "./repos";

// not sure if it is required
export type {
  File,
  FileChangeType,
  Hunk,
  Change,
  BaseContext,
  AnnotationFactory,
  AnnotationFactoryContext,
  DiffEventHandler,
  DiffEventContext
} from "./repos";
