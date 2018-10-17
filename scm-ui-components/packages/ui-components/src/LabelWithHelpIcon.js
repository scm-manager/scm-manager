//@flow
import React from "react";
import { Help } from "./index";

type Props = {
  label: string,
  helpText?: string
};

class LabelWithHelpIcon extends React.Component<Props> {
  renderLabel = () => {
    const label = this.props.label;
    if (label) {
      return <label className="label">{label}</label>;
    }
    return "";
  };

  renderHelp = () => {
    const helpText = this.props.helpText;
    if (helpText) {
      return (
        <div className="control columns is-vcentered">
          <Help message={helpText} />
        </div>
      );
    } else return null;
  };

  renderLabelWithHelpIcon = () => {
    if (this.props.label) {
      return (
        <div className="field is-grouped">
          <div className="control">{this.renderLabel()}</div>
          {this.renderHelp()}
        </div>
      );
    } else return null;
  };

  render() {
    return this.renderLabelWithHelpIcon();
  }
}

export default LabelWithHelpIcon;
