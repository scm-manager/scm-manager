//@flow
import React from "react";
import { withContextPath } from "./urls";

type Props = {
  src: string,
  alt: string,
  className?: any
};

class Image extends React.Component<Props> {
  render() {
    const { src, alt, className } = this.props;
    return <img className={className} src={withContextPath(src)} alt={alt} />;
  }
}

export default Image;
