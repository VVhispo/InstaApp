const http = require('http');
const imageRouter = require("./app/images/imageRouter")
const tagsRouter = require("./app/tags/tagsRouter")
const {init} = require("./app/tags/jsonController")

http
    .createServer(async (req, res) => {   
        if (req.url.search("/api/photos") != -1) {
           await imageRouter(req, res)
        }
        else if (req.url.search("/api/tags") != -1) {
           await tagsRouter(req, res)
        }
    })
    .listen(3000, () => {
         init();
         console.log("listening on 3000")
    })