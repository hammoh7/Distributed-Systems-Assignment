from flask import Flask, request 
 
app = Flask(__name__) 
 
@app.route("/add", methods=["POST"]) 
def add(): 
    data = request.get_json() 
    result = data["num1"] + data["num2"] 
    return str(result) 
 
@app.route("/subtract", methods=["POST"]) 
def subtract(): 
    data = request.get_json() 
    result = data["num1"] - data["num2"] 
    return str(result) 
 
@app.route("/multiply", methods=["POST"]) 
def multiply(): 
    data = request.get_json() 
    result = data["num1"] * data["num2"] 
    return str(result) 
 
@app.route("/divide", methods=["POST"]) 
def divide(): 
    data = request.get_json() 
    result = data["num1"] / data["num2"] 
    return str(result) 
 
if __name__ == "__main__": 
    app.run(debug=True) 