const path = require('path')
const fs = require('fs')
const formidable = require("formidable");

module.exports = {
    saveFile: async(request, response, profilePic) => {
        const form = formidable.IncomingForm()
        return new Promise((resolve)=>{
            form.parse(request, async(err, fields, files) => {
                console.log("saving file")
                if(err) return null
                const uploadFolder = path.join(__dirname,"../../uploads",fields.album);
                if (!fs.existsSync(uploadFolder)){
                    fs.mkdirSync(uploadFolder, { recursive: true });
                }
                form.uploadDir = uploadFolder
                const file = files.file
                if(!file){
                    console.log("file undefined")
                    return;
                }
                const ext = "."+ file.name.split(".")[file.name.split(".").length - 1]
                let new_path;
                if(profilePic) new_path = path.join(uploadFolder, "user_profile_pic.jpg")
                else new_path = path.join(uploadFolder, "upload_" + Date.now().toString() + ext)
                try {
                    fs.copyFile(file.path, new_path, function(err){
                        if(err) console.log(err)
                    });
                    resolve({
                        album: fields.album,
                        originalName: file.name,
                        url: new_path
                    })
                  } catch (error) {
                    console.log(error)
                }    
            })
        })
    },
    deleteFile: (url) => {
        fs.unlinkSync(url)
    },
    readPhoto: (url) => {
        return new Promise((resolve, reject)=>{
            fs.readFile(url, (err, data) => {
                if(err) console.log(err);
                else resolve(data)
            })
        })
    }
}