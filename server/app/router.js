const getRequestData = require ('./getRequestData')
const {saveFile, deleteFile} = require("./fileController")
const {addPhoto, getPhotos, getPhoto, delPhoto, patchPhoto} = require("./jsonController")


const router = async (request, response) => {
    switch (request.method) {
        case "GET":
            response.writeHead(200, {'Content-Type': 'application/json'})
            if(request.url == "/api/photos"){
                response.write(getPhotos())
            }else if(request.url.match(/\/api\/photos\/([0-9]+)/)){
                const photo = getPhoto(request.url.split("/")[request.url.split("/").length - 1])
                if(photo) response.write(photo)
                else{
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found!")
                }
            }
            break;
        case "POST":
            response.writeHead(201, {'Content-Type': 'application/json'})
            if(request.url == "/api/photos"){
                const uploadData = await saveFile(request, response)
                const newPhoto = addPhoto(uploadData)
                response.write(newPhoto)
            }
            break;
        case "DELETE":
            response.writeHead(200, {'Content-Type': 'text/html'})
            if(request.url.match(/\/api\/photos\/([0-9]+)/)){
                const urlDeleted = delPhoto(request.url.split("/")[request.url.split("/").length - 1])
                if(urlDeleted){
                    deleteFile(urlDeleted)
                    response.write("Successfuly deleted photo with id " + request.url.split("/")[request.url.split("/").length - 1])
                }else{
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found")
                }
                
            }
            break;
        case "PATCH":
            response.writeHead(200, {'Content-Type': 'text/html'})
            if(request.url == "/api/photos"){
                const data = await getRequestData(request)
                const idPatched = patchPhoto(JSON.parse(data))
                if(idPatched){ response.write("Successfuly patched photo with id " + idPatched) }
                else {
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found")
                }
            }
            break;
    }
    response.end()
}

module.exports = router