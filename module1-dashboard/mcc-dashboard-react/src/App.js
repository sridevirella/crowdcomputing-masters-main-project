import React, {useState} from "react";
import './App.css';
import SignIn from "./components/SignIn.js";
import SimpleTabs from "./components/Tabs.js";
import Button from '@material-ui/core/Button';


function App() {
  const [isLoggedInParent, setIsLoggedInParent] = useState(false);

  //shows child component data in parent component
  const sendDataToParent = (childData) => {
    setIsLoggedInParent(childData);
  };

  return (
      <div className="App">
        { isLoggedInParent ? <Button variant="contained" size="medium"  onClick={() => setIsLoggedInParent(false)} className="logout-button" color="secondary"> Logout </Button> : ""}
        { !isLoggedInParent ? <SignIn sendDataToParent= { sendDataToParent } /> : <SimpleTabs /> }
      </div>
  );
}
export default App;
