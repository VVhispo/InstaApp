const getRequestData = require ('../getRequestData')
const {saveFile, deleteFile} = require("../controllers/fileController")
const IC = require("../controllers/imageController") 
// const {addPhoto, getPhotos, getPhoto, delPhoto, patchPhoto, addTag, getTags, addTags} = require("../controllers/imageController")


const imageRouter = async (request, response) => {
    switch (request.method) {
        case "GET":
            response.writeHead(200, {'Content-Type': 'application/json'})
            if(request.url == "/api/photos"){
                response.write(IC.getPhotos())
            }else if(request.url.match(/\/api\/photos\/([0-9]+)/)){
                const photo = IC.getPhoto(request.url.split("/")[request.url.split("/").length - 1])
                if(photo) response.write(photo)
                else{
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found!")
                }
            }else if(request.url.match(/\/api\/photos\/tags\/([0-9]+)/)){
                const tags = IC.getTags(request.url.split("/")[request.url.split("/").length - 1])
                if(tags) response.write(tags)
                else{
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found!")
                }
            }else if(request.url.match(/\/api\/photos\/([a-zA-Z]+)/)){
                const photos = IC.getPhotosFromFolder(request.url.split("/")[3])
                if(photos) response.write(photos)
                else{
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("No photos found!")
                }
            }
            break;
        case "POST":
            response.writeHead(201, {'Content-Type': 'application/json'})
            if(request.url == "/api/photos"){
                const uploadData = await saveFile(request, response)
                const newPhoto = IC.addPhoto(uploadData)
                response.write(newPhoto)
            }
            break;
        case "DELETE":
            response.writeHead(200, {'Content-Type': 'text/html'})
            if(request.url.match(/\/api\/photos\/([0-9]+)/)){
                const urlDeleted = IC.delPhoto(request.url.split("/")[request.url.split("/").length - 1])
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
                const idPatched = IC.patchPhoto(JSON.parse(data))
                if(idPatched){ response.write("Successfuly patched photo with id " + idPatched) }
                else {
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found")
                }
            }else if(request.url == "/api/photos/tags"){
                const data = await getRequestData(request)
                const photo = IC.addTag(JSON.parse(data))
                if(photo) response.write(photo)
                else{
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found")
                }
            }else if(request.url == "/api/photos/tags/mass"){
                const data = await getRequestData(request)
                const photo = IC.addTags(JSON.parse(data))
                if(photo) response.write(photo)
                else{
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write("ID not found")
                }
            }
            break;
    }
    response.end()
}

module.exports = imageRouter