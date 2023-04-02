const {getRequestData} = require ('./getRequestData')
const formidable = require("formidable");
const {photosArray} = require("./model")
const {saveFile} = require("./fileController")


const router = async (request, response) => {

    switch (request.method) {
        case "GET":
            break;
        case "POST":
            if(request.url == "/api/photos"){
                saveFile(formidable.IncomingForm(), request)
                console.log(photosArray)
            }
            break;

    }
}

module.exports = router