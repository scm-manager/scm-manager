//@flow
import React from "react";
import Image from "../images/logo.png";

type Props = {};

class Logo extends React.Component<Props> {
  render() {
    return <img src={Image} alt="SCM-Manager logo" />;
  }
}

export default Logo;
