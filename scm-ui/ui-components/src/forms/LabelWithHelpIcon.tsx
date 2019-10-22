import React from "react";
import Help from "../Help";

type Props = {
  label?: string;
  helpText?: string;
};

class LabelWithHelpIcon extends React.Component<Props> {
  renderHelp() {
    const { helpText } = this.props;
    if (helpText) {
      return <Help message={helpText} />;
    }
  }

  render() {
    const { label } = this.props;

    if (label) {
      const help = this.renderHelp();
      return (
        <label className="label">
          {label} {help}
        </label>
      );
    }

    return "";
  }
}

export default LabelWithHelpIcon;
