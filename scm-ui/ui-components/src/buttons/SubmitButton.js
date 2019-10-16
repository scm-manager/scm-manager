//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";

type SubmitButtonProps = ButtonProps & {
  scrollToTop: boolean
}

class SubmitButton extends React.Component<SubmitButtonProps> {
  static defaultProps = {
    scrollToTop: true
  };

  render() {
    const { action, scrollToTop } = this.props;
    return (
      <Button
        type="submit"
        color="primary"
        {...this.props}
        action={(event) => {
          if (action) {
            action(event);
          }
          if (scrollToTop) {
            window.scrollTo(0, 0);
          }
        }}
      />
    );
  }
}

export default SubmitButton;
