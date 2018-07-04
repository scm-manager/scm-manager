//@flow
import React from 'react';
import injectSheet from 'react-jss';

const styles = {
  wrapper: {
    width: '100%',
    display: 'flex',
    height: '10em'
  },
  login: {
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
        <div className={classes.login}>
          You need to log in! ...
        </div>
      </div>
    );
  }

}

export default injectSheet(styles)(Login);
