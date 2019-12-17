// @create-index

import * as validation from "./validation";
import * as urls from "./urls";
import * as repositories from "./repositories";

// not sure if it is required
import {
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

export { validation, urls, repositories };

export { default as DateFromNow } from "./DateFromNow";
export { default as ErrorNotification } from "./ErrorNotification";
export { default as ErrorPage } from "./ErrorPage";
export { default as Icon } from "./Icon";
export { default as Image } from "./Image";
export { default as Loading } from "./Loading";
export { default as Logo } from "./Logo";
export { default as MailLink } from "./MailLink";
export { default as Notification } from "./Notification";
export { default as Paginator } from "./Paginator";
export { default as LinkPaginator } from "./LinkPaginator";
export { default as StatePaginator } from "./StatePaginator";

export { default as FileSize } from "./FileSize";
export { default as ProtectedRoute } from "./ProtectedRoute";
export { default as Help } from "./Help";
export { default as HelpIcon } from "./HelpIcon";
export { default as Tag } from "./Tag";
export { default as Tooltip } from "./Tooltip";
// TODO do we need this? getPageFromMatch is already exported by urls
export { getPageFromMatch } from "./urls";
export { default as Autocomplete } from "./Autocomplete";
export { default as GroupAutocomplete } from "./GroupAutocomplete";
export { default as UserAutocomplete } from "./UserAutocomplete";
export { default as BranchSelector } from "./BranchSelector";
export { default as Breadcrumb } from "./Breadcrumb";
export { default as MarkdownView } from "./MarkdownView";
export { default as SyntaxHighlighter } from "./SyntaxHighlighter";
export { default as ErrorBoundary } from "./ErrorBoundary";
export { default as OverviewPageActions } from "./OverviewPageActions";
export { default as CardColumnGroup } from "./CardColumnGroup";
export { default as CardColumn } from "./CardColumn";

export { default as comparators } from "./comparators";

export { apiClient } from "./apiclient";
export * from "./errors";

export * from "./avatar";
export * from "./buttons";
export * from "./config";
export * from "./forms";
export * from "./layout";
export * from "./modals";
export * from "./navigation";
export * from "./repos";
export * from "./table";
export * from "./toast";

export {
  File,
  FileChangeType,
  Hunk,
  Change,
  BaseContext,
  AnnotationFactory,
  AnnotationFactoryContext,
  DiffEventHandler,
  DiffEventContext
};
