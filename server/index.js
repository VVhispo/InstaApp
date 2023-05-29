const http = require('http');
const imageRouter = require("./app/routers/imageRouter")
const tagsRouter = require("./app/routers/tagsRouter")
const filtersRouter = require("./app/routers/filtersRouter")
const usersRouter = require("./app/routers/usersRouter")
const {init} = require("./app/controllers/tagsController")
require('dotenv').config();

http
    .createServer(async (req, res) => {   
        if (req.url.search("/api/photos") != -1) {
            await imageRouter(req, res)
        }else if (req.url.search("/api/tags") != -1) {
            await tagsRouter(req, res)
        }else if(req.url.search("/api/filters") != -1){
            await filtersRouter(req, res)
        }else if(req.url.search("/api/users") != -1){
            await usersRouter(req, res)
        }
    })
    .listen(process.env.APP_PORT, () => {
         init()
         console.log("listening on " + process.env.APP_PORT)
    })