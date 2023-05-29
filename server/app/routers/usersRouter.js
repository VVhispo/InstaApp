const getRequestData = require ('../getRequestData')
const UC = require("../controllers/userController")

const usersRouter = async(request, response) => {
    switch(request.method){
        case "GET":
            response.writeHead(200, {'Content-Type':'application/json'})
            if(request.url == "/api/users"){
                const users = UC.getUsers()
                response.write(users)
            }
            else if(request.url.match(/\/api\/users\/confirm\/(.*)/)){
                const token = request.url.split("/")[request.url.split("/").length - 1]
                console.log(token)
                const result = await UC.verifyUser(token)
                if(JSON.parse(result).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(result)
            }
            break;
        case "POST":
            response.writeHead(201, {'Content-Type':'application/json'})
            if(request.url == "/api/users/register"){       
                const data = await getRequestData(request)
                const verify_data = await UC.checkUser(data)
                if(JSON.parse(verify_data)){
                    response.writeHead(404, {'Content-Type':'application/json'})
                    response.write(verify_data)
                    break;
                }
                const token = await UC.registerUser(data);
                response.write(JSON.stringify({message:`Use this link to verify your account: \nhttp://localhost:3000/api/users/confirm/${JSON.parse(token).token}`}))
            }else if(request.url == "/api/users/login"){
                const data = await getRequestData(request)
                const token = await UC.loginUser(data)
                if(JSON.parse(token).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(token)
            }
            break;
    }
    response.end()
}

module.exports = usersRouter