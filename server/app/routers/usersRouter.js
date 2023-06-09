const getRequestData = require ('../getRequestData')
const UC = require("../controllers/userController")
const {saveFile, readPhoto} = require("../controllers/fileController")

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
                const result = await UC.verifyUser(token)
                if(JSON.parse(result).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(result)
            }else if(request.url == "/api/users/profile" && request.headers.authorization
            && request.headers.authorization.startsWith("Bearer")){
                const token = request.headers.authorization.split(" ")[1]
                const result = await UC.getUserProfile(token)
                if(JSON.parse(result).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(result)
            }else if(request.url == "/api/users/profilePic" && request.headers.authorization
            && request.headers.authorization.startsWith("Bearer")){
                const token = request.headers.authorization.split(" ")[1]
                const result = await UC.getUserProfile(token)
                if(JSON.parse(result).error) {
                    response.writeHead(404, {'Content-Type': 'application/json'})
                    response.write(result)
                    break;
                }
                const pic = await readPhoto(JSON.parse(result).profilePicUrl)
                response.writeHead(200, {'Content-type':'image/jpeg'})
                response.write(pic)
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
                response.write(JSON.stringify({"token": JSON.parse(token).token}))
            }else if(request.url == "/api/users/login"){
                const data = await getRequestData(request)
                const token = await UC.loginUser(data)
                if(JSON.parse(token).error) response.writeHead(404, {'Content-Type': 'application/json'})
                response.write(token)
            }else if(request.url == "/api/users/setProfilePic"){
                const uploadData = await saveFile(request, response, true)
                console.log(uploadData)
                const user = await UC.setUserProfilePic(uploadData)
                // const res = await readPhoto(JSON.parse(user).user.profilePicUrl)
                // console.log(res)
                response.write(JSON.stringify({"message":"success"}))
            }
            break;
        case "PATCH":
            response.writeHead(200, {"Content-type":"application/json"})
            if(request.url == "/api/users" && request.headers.authorization
            && request.headers.authorization.startsWith("Bearer")){
                const token = request.headers.authorization.split(" ")[1]
                const data = await getRequestData(request)
                const user = await UC.patchUserProfile(token, JSON.parse(data));
                if(JSON.parse(user).error) response.writeHead(404, {"Content-type":"application/json"})
                response.write(user)
            }
    }
    response.end()
}

module.exports = usersRouter