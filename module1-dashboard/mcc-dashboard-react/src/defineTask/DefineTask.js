import React from "react";
import "./defineTask.css"
import axios from "axios";
import "react-datepicker/dist/react-datepicker.css";
import "react-datetime/css/react-datetime.css";
import Datetime from "react-datetime";
import moment from "moment";

//Allows the user to define a task with set of properties and uploading a .txt or executable file.
export default class DefineTask extends React.Component {

    constructor(props){
        super(props);
        this.onSubmit = this.onSubmit.bind(this);
        this.setStartDate = this.setStartDate.bind(this);
        this.formatDate = this.formatDate.bind(this);
        this.constructJsonObject= this.constructJsonObject.bind(this);
        this.state = {
            dateValue: new Date(),
            finalDate: "",
            file: null
        };

    }

    //Set input field due date state
    setStartDate(date) {
        this.setState({
            dateValue: new Date(date),
            finalDate: this.formatDate(date)
        });
    }

    //Format date
    formatDate(date) {
        return moment(date).format('MM DD YYYY HH mm ss');
    }

    // Format and set time state
    setTime(time) {
        let hours = time.getHours();
        let minutes = time.getMinutes();
        let strTime = hours + ' ' + minutes

        this.setState({
            timeValue: new Date(time),
            finalTime: strTime
        }, () => {
            console.log(" time value = "+this.state.timeValue);
        });
    }

    //Construct task description JSON
    constructJsonObject ( shortName, description , dueDate, size, author, rewards ) {

        const taskDescriptionSingleObject = {};

        taskDescriptionSingleObject.shortName = shortName;
        taskDescriptionSingleObject.description = description;
        taskDescriptionSingleObject.dueDate = dueDate;
        taskDescriptionSingleObject.size = size;
        taskDescriptionSingleObject.author = author;
        taskDescriptionSingleObject.rewards = rewards;

        return JSON.stringify(taskDescriptionSingleObject);
    }

    // Set the input field upload file state
    onChangeHandler = (event) => {
        this.setState({file:event.target.files[0]})
    }

    // Reset the form fields after successful task submission.
    resetFormFields = () => {
        document.getElementById("define-task-form").reset();
        document.getElementById('myFile').value = null;
    }

    //Construct HTTP request body with task details and post request to the node server through axios.post
    onSubmit(e) {
        e.preventDefault();
        let shortName = this.shortName.value;
        let description = this.description.value;
        let size = this.size.value;
        let author = this.author.value;
        let rewards = this.rewards.value;
        let dueDate = this.state.finalDate;
        let requestBody = this.constructJsonObject(shortName, description , dueDate, size, author, rewards );

        let formData = new FormData();
        formData.append('executableFile',  this.state.file);
        formData.append('taskDetails', requestBody);
        console.log("Initiated task details = " + JSON.stringify(requestBody));

        let  headers= {
            "Access-Control-Allow-Origin": "http://localhost:3000",
            "Content-Type": "application/json",
            "Access-Control-Allow-Credentials" : true,
            "Access-Control-Allow-Methods": "DELETE, POST, GET, OPTIONS",
            "Access-Control-Allow-Headers": "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With"
        }
        
        axios.post("http://localhost:4000/postTask",formData, headers ) //API end point to sned task details to node server
            .then(function (response) {
                alert('Task successfully submitted')
            })
            .catch((error) => {
                console.log('👉 Error while Api call":', error);
            });
        this.resetFormFields();
    }

    //Handles define Task component UI elements.
    render(){

        return (
            <div className="define-container">
                <div class="container-shadow"></div>
                <div class="container">
                    <div class="wrap">
                        <div class="headings">
                            <a id="define-task" href="#" class="active"><span>Define Task</span></a>
                        </div>
                        <div id="sign-in-form">

                            <form className="form-horizontal" id="define-task-form">
                                <div className="left" >
                                    <label>Short Name : <input type="text" className="form-control"  ref={(c) => this.shortName = c} name="shortName" autocomplete = "off" /></label>
                                    <label>Description : <textarea  className="form-control"  ref={(c) => this.description = c} name="description" autocomplete = "off" /></label>
                                    <label>Due Date : <br /> </label>
                                    <div className="picker-font-color"><Datetime
                                        selected={this.state.dateValue}
                                        onChange={date => this.setStartDate(date)}/>
                                    </div>
                                </div>
                                <div className="right">
                                    <label>Size : <input type="text" className="form-control" ref={(c) => this.size = c} name="size"  autocomplete = "off" /></label>
                                    <label>Author : <br /><br /><input type="text" className="form-control" ref={(c) => this.author = c} name="author"  autocomplete = "off" /></label>
                                    <label>Rewards : <input type="text" className="form-control" ref={(c) => this.rewards = c} name="rewards" autocomplete = "off" /></label>
                                </div>
                                <div className="card-container action-buttons">
                                    <form action="" id = "selected-workers-formid">
                                        <input type="file" id="myFile" name="filename" onChange = {this.onChangeHandler} />

                                    </form>
                                </div>
                            </form>
                            <input type="submit" onClick={this.onSubmit} class="button" name="submit" value="Submit" />
                        </div>
                        {this.props.taskDetails ?
                            (<div>
                                { this.props.taskDetails.name },
                            </div>)
                            : <span></span>}
                    </div>
                </div>
            </div>
        )}
};