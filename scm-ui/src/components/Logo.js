//@flow
import React from "react";
import Image from "../images/logo.png";

class Logo extends React.PureComponent {
  render() {
    return <img src={Image} alt="SCM-Manager logo" />;
  }
}

export default Logo;
