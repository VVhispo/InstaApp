const path = require('path')
const fs = require('fs')
const formidable = require("formidable");

module.exports = {
    saveFile: async(request, response, profilePic) => {
        const form = formidable.IncomingForm()
        return new Promise((resolve)=>{
            form.parse(request, async(err, fields, files) => {
                console.log(fields, files)
                if(err) return null
                const uploadFolder = path.join(__dirname,"../../uploads",fields.album);
                if (!fs.existsSync(uploadFolder)){
                    fs.mkdirSync(uploadFolder, { recursive: true });
                }
                form.uploadDir = uploadFolder
                const file = files.file
                let new_path;
                if(profilePic) new_path = path.join(uploadFolder, "user_profile_pic.jpg")
                else new_path = path.join(uploadFolder, "upload_" + Date.now().toString() + ".jpg")
                try {
                    fs.copyFile(file.path, new_path, function(err){
                        if(err) throw err
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
        console.log(url)
        return new Promise((resolve, reject)=>{
            fs.readFile(url, (err, data) => {
                if(err) console.log(err);
                else resolve(data)
            })
        })
    },
}