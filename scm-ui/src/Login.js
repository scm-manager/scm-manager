//@flow
import React from 'react';
import injectSheet from 'react-jss';

const styles = {
  wrapper: {
    width: '100%',
    display: 'flex',
    height: '10em'
  },
  loading: {
    margin: 'auto',
    textAlign: 'center'
  }
};

type Props = {
  classes: any;
}

class Login extends React.Component<Props> {

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.wrapper}>
        <div className={classes.loading}>
          You need to log in! ...
        </div>
      </div>
    );
  }

}

export default injectSheet(styles)(Login);
