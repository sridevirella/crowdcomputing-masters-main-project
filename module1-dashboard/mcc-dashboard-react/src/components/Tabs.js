import React, {useEffect} from 'react';
import PropTypes from 'prop-types';
import {makeStyles} from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import DefineTask from "../defineTask/DefineTask.js";
import openSocket from 'socket.io-client';

//Handles Define task, result tab UI display, navigation and render through material-ui

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
      <div
          role="tabpanel"
          hidden={value !== index}
          id={`simple-tabpanel-${index}`}
          aria-labelledby={`simple-tab-${index}`}
          {...other}>
        {value === index && (
            <Box p={3}>
              <Typography>{children}</Typography>
            </Box>
        )}
      </div>
  );
}

//validate the children components and its types
TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};

function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
}

//tab panel display style
const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  },
}));

export default function SimpleTabs() {
  const classes = useStyles();
  const [value, setValue] = React.useState(0);
  const [responseData, setResponseData] = React.useState(0);
  const [imageURL, setImageURL] = React.useState(0);
  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  // Receives the emitted messages from the node server and set received message data to the responseData.
  useEffect( () => {
    const socket = openSocket('http://localhost:4000', {transports:['websocket']});
    socket.on("FromAPI", data => {
      console.log('socket response here:', data);
      setResponseData(data);
    });
  });

  //Handles both define task and Result tabs display
  return (
      <div className={classes.root}>
        <AppBar size="small" position="static">
          <Tabs value={value} onChange={handleChange} aria-label="tabs" centered>
            <Tab label="Define Task" {...a11yProps(0)} />
            <Tab label="Result" {...a11yProps(1)} />
          </Tabs>
        </AppBar>
        <TabPanel value={value} index={0}>
          <DefineTask  />
        </TabPanel>
        <TabPanel value={value} index={1}>
           <br/>
          {responseData || "" }
        </TabPanel>
      </div>
  );
}
