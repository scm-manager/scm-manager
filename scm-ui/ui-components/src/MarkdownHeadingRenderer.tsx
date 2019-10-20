import React, { ReactNode } from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
import { withContextPath } from "./urls";

/**
 * Adds anchor links to markdown headings.
 *
 * @see <a href="https://github.com/rexxars/react-markdown/issues/69">Headings are missing anchors / ids</a>
 */

type Props = RouteComponentProps & {
  children: ReactNode;
  level: number;
};

function flatten(text: string, child: any): any {
  return typeof child === "string"
    ? text + child
    : React.Children.toArray(child.props.children).reduce(flatten, text);
}

/**
 * Turns heading text into a anchor id
 *
 * @VisibleForTesting
 */
export function headingToAnchorId(heading: string) {
  return heading.toLowerCase().replace(/\W/g, "-");
}

function MarkdownHeadingRenderer(props: Props) {
  const children = React.Children.toArray(props.children);
  const heading = children.reduce(flatten, "");
  const anchorId = headingToAnchorId(heading);
  const headingElement = React.createElement(
    "h" + props.level,
    {},
    props.children
  );
  const href = withContextPath(props.location.pathname + "#" + anchorId);

  return (
    <a id={`${anchorId}`} className="anchor" href={href}>
      {headingElement}
    </a>
  );
}

export default withRouter(MarkdownHeadingRenderer);
