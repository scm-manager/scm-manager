import React from "react";
import { withContextPath } from "./urls";

type Props = {
  src: string;
  alt: string;
  className?: any;
};

class Image extends React.Component<Props> {
  createImageSrc = () => {
    const { src } = this.props;
    if (src.startsWith("http")) {
      return src;
    }
    return withContextPath(src);
  };

  render() {
    const { alt, className } = this.props;
    return <img className={className} src={this.createImageSrc()} alt={alt} />;
  }
}

export default Image;
