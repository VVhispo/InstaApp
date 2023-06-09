const getRequestData = require ('../getRequestData')
const {saveFile, deleteFile, readPhoto} = require("../controllers/fileController")
const IC = require("../controllers/imageController") 


const imageRouter = async (request, response) => {
    switch (request.method) {
        case "GET":
            response.writeHead(200, {'Content-Type': 'application/json'})
            if(request.url == "/api/photos"){
                response.write(IC.getPhotos())
            }else if(request.url.match(/\/api\/photos\/([0-9]+)/)){
                const photo = IC.getPhoto(request.url.split("/")[request.url.split("/").length - 1])
                if(JSON.parse(photo).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(photo)
            }else if(request.url.match(/\/api\/photos\/tags\/([0-9]+)/)){
                const tags = IC.getTags(request.url.split("/")[request.url.split("/").length - 1])
                if(JSON.parse(tags).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(tags)
            }else if(request.url.match(/\/api\/photos\/getfile_filtered\/([0-9]+)/)){
                const url = IC.getFilteredUrl(request.url.split("/")[request.url.split("/").length - 2])
                if(JSON.parse(url).error){
                    response.writeHead(404, {'Content-Type': 'application/json'})
                    response.write(url)
                    break
                }
                const res = await readPhoto(url)
                if(!res){
                    response.writeHead(404, {'Content-Type': 'application/json'})
                    response.write(JSON.stringify({error: "Error while reading file!"}))
                }
                response.writeHead(200, {'Content-type':'image/jpeg'})
                response.write(res)
            }else if(request.url.match(/\/api\/photos\/getfile\/([0-9]+)/)){
                const photo = IC.getPhoto(request.url.split("/")[request.url.split("/").length - 1])
                if(JSON.parse(photo).error){
                    response.writeHead(404, {'Content-Type': 'application/json'})
                    response.write(photo)
                    break;
                }
                const res = await readPhoto(JSON.parse(photo).url)
                if(!res){
                    response.writeHead(404, {'Content-Type': 'application/json'})
                    response.write(JSON.stringify({error: "Error while reading file!"}))
                }
                response.writeHead(200, {'Content-type':'image/jpeg'})
                response.write(res)
            }else if(request.url.match(/\/api\/photos\/([a-zA-Z]+)/)){
                const photos = IC.getPhotosFromFolder(request.url.split("/")[3])
                if(JSON.parse(photos).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(photos)
            }
            break;
        case "POST":
            response.writeHead(201, {'Content-Type': 'application/json'})
            if(request.url == "/api/photos"){
                const uploadData = await saveFile(request, response, false)
                const newPhoto = IC.addPhoto(uploadData)
                response.write(newPhoto)
            }else if(request.url == "/api/photos/location"){
                const data = await getRequestData(request)
                const photo = IC.setLocation(JSON.parse(data))
                if(JSON.parse(photo).error){
                    response.writeHead(404, {'Content-Type': 'application/json'})
                }
                response.write(photo)
            }
            break;
        case "DELETE":
            response.writeHead(200, {'Content-Type': 'application/json'})
            if(request.url.match(/\/api\/photos\/([0-9]+)/)){
                const urlDeleted = IC.delPhoto(request.url.split("/")[request.url.split("/").length - 1])
                if(!JSON.parse(urlDeleted).error){
                    deleteFile(JSON.parse(urlDeleted))
                    response.write(JSON.stringify({message:"Successfuly deleted photo with id " + request.url.split("/")[request.url.split("/").length - 1]}))
                }else{
                    response.writeHead(404, {'Content-Type': 'application json'})
                    response.write(urlDeleted)
                }
                
            }
            break;
        case "PATCH":
            response.writeHead(200, {'Content-Type': 'application/json'})
            if(request.url == "/api/photos"){
                const data = await getRequestData(request)
                const idPatched = IC.patchPhoto(JSON.parse(data))
                if(JSON.parse(idPatched).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(idPatched)
            }else if(request.url == "/api/photos/tags"){
                const data = await getRequestData(request)
                const photo = IC.addTag(JSON.parse(data))
                if(JSON.parse(photo).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(photo)
            }else if(request.url == "/api/photos/tags/mass"){
                const data = await getRequestData(request)
                const photo = IC.addTags(JSON.parse(data))
                if(JSON.parse(photo).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(photo)
            }
            break;
    }
    response.end()
}

module.exports = imageRouter