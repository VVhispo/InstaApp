const http = require('http');
const imageRouter = require("./app/routers/imageRouter")
const tagsRouter = require("./app/routers/tagsRouter")
const filtersRouter = require("./app/routers/filtersRouter")
const {init} = require("./app/controllers/tagsController")

http
    .createServer(async (req, res) => {   
        if (req.url.search("/api/photos") != -1) {
            await imageRouter(req, res)
        }else if (req.url.search("/api/tags") != -1) {
            await tagsRouter(req, res)
        }else if(req.url.search("/api/filters") != -1){
            await filtersRouter(req, res)
        }
    })
    .listen(3000, () => {
         init();
         console.log("listening on 3000")
    })