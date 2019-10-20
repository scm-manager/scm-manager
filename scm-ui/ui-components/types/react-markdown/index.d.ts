/// <reference types="react-markdown" />

/**
 * fix missing type for react-markdown/with-html
 * @see https://github.com/rexxars/react-markdown/pull/352
 */

declare module "react-markdown/with-html" {
  import { Component } from "react";
  import ReactMarkdownRoot from "react-markdown";

  export default class ReactMarkdown extends Component<
    ReactMarkdownRoot.ReactMarkdownProps,
    {}
  > {}
}
