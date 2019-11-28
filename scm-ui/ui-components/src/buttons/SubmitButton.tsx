import React, { MouseEvent } from "react";
import Button, { ButtonProps } from "./Button";

type SubmitButtonProps = ButtonProps & {
  scrollToTop: boolean;
};

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
        action={(event: MouseEvent) => {
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
