const FC = require('../controllers/filtersController')
const getRequestData = require ('../getRequestData')

const filtersRouter = async(request, response) => {
    response.writeHead(200, { "content-type": "application/json" })
    if(request.method == "GET" && request.url.match(/\/api\/filters\/metadata\/([0-9]+)/)){
        const metadata = await FC.getPhotoMeta(request.url.split("/")[4])
        if(JSON.parse(metadata).error) response.writeHead(404, {'Content-Type': 'application/json'})
        response.write(metadata)
    }else if(request.method == "PATCH"  && request.url == "/api/filters"){
        const data = await getRequestData(request)
        const filtered = await FC.applyFilter(JSON.parse(data))
        if(JSON.parse(filtered).error) response.writeHead(404, {'Content-Type': 'application/json'})
        response.write(filtered)
    }
    response.end()
}
module.exports = filtersRouter