const getRequestData = require ('../getRequestData')
const TC = require("../controllers/tagsController")

const tagsRouter = async (request, response) => {
    switch (request.method) {
        case "GET":
            response.writeHead(200, {'Content-Type': 'application/json'})
            if(request.url == "/api/tags/raw"){
                response.write(TC.getRawTags())
            }else if(request.url == "/api/tags"){
                response.write(TC.getTags())
            }else if(request.url == "/api/tags/popular"){
                response.write(TC.getTagsPopular())
            }else if(request.url.match(/\/api\/tags\/([0-9]+)/)){
                const id = request.url.split("/")[request.url.split("/").length - 1]
                const tag = TC.getTag(id)
                if(JSON.parse(tag).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(tag)
            }
            break;
        case "POST":
            response.writeHead(201, {'Content-Type': 'application/json'})
            if(request.url == "/api/tags"){
                const data = await getRequestData(request)
                const newTag = TC.addTag(JSON.parse(data))
                if(JSON.parse(newTag).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(newTag)
            }
    }
    response.end()
}

module.exports = tagsRouter