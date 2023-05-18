const getRequestData = require ('../getRequestData')
const FC = require('../controllers/filtersController')

const filtersRouter = async(request, response) => {
    response.writeHead(200, { "content-type": "application/json" })
    if(request.method == "GET" && request.url.match(/\/api\/filters\/metadata\/([0-9]+)/)){
        const metadata = await FC.getPhotoMeta(request.url.split("/")[4])
        if(!metadata){
            response.writeHead(404, { "content-type": "text/html" })
            response.write("Error! Not found")
        }else{
            response.write(JSON.stringify(metadata))
        }
    }else if(request.method == "PATCH"  && request.url == "/api/filters"){
        const filtered = await FC.applyFilter(request)
        if(!filtered){
            response.writeHead(404, { "content-type": "text/html" })
            response.write("Error!")
        }else{
            response.write(filtered)
        }
    }
    response.end()
}
module.exports = filtersRouter