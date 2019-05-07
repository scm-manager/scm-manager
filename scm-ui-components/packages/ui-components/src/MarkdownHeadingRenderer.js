// @flow
import * as React from "react";

/**
 * Adds anchor links to markdown headings.
 *
 * @see <a href="https://github.com/rexxars/react-markdown/issues/69">Headings are missing anchors / ids</a>
 */

type Props = {
  children: React.Node,
  level: number
};

function flatten(text: string, child: any) {
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

export default function MarkdownHeadingRenderer(props: Props) {
  const children = React.Children.toArray(props.children);
  const heading = children.reduce(flatten, "");
  const anchorId = headingToAnchorId(heading);
  return React.createElement("h" + props.level, {id: anchorId}, props.children);
}
