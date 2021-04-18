import React from 'react';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import {makeStyles} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';


const useStyles = makeStyles((theme) => ({
  paper: {
    marginTop: theme.spacing(8),
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'Center',
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%',
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
}));

//Define task component.
export default function TaskForm() {
  const classes = useStyles();

  return (
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <div className={classes.paper}>
          <form className={classes.form} post="" noValidate>
            <Grid container spacing={2}>
              <Grid item xs={12} >
                <TextField
                    autoComplete="sname"
                    name="shortName"
                    variant="filled"
                    fullWidth
                    id="shortName"
                    label="Short Name"
                    autoFocus
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                    variant="filled"
                    fullWidth
                    id="description"
                    label="Description"
                    name="description"
                    autoComplete="ldesc"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                    id="datetime-local"
                    label="Due Date"
                    type="datetime-local"
                    defaultValue="2020-06-12T10:30"
                    className={classes.textField}
                    InputLabelProps={{
                      shrink: true,
                    }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                    variant="filled"
                    fullWidth
                    name="taskSize"
                    label="Size"
                    id="taskSize"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                    variant="filled"
                    fullWidth
                    name="author"
                    label="Author Name"
                    id="author"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                    variant="filled"
                    fullWidth
                    name="rewards"
                    label="Rewards"
                    id="rewards"
                />
              </Grid>
            </Grid>
            <Button
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                className={classes.submit}>
              Submit
            </Button>
          </form>
        </div>
      </Container>
  );
}